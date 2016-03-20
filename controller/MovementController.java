package controller;

import java.util.Vector;

import controller.pathfinding.Pathfinding;
import controller.pathfinding.Pathfinding.Direction;
import gamedata.d2p.MapsCache;
import gamedata.d2p.ankama.Map;
import messages.EmptyMessage;
import messages.context.ChangeMapMessage;
import messages.context.GameMapMovementRequestMessage;
import messages.interactions.NpcDialogReplyMessage;
import messages.interactions.NpcGenericActionRequestMessage;

public class MovementController {
	private CharacterController character;
	private Pathfinding pathfinding;
	
	public MovementController(CharacterController character) {
		this.character = character;
		this.pathfinding = new Pathfinding();
	}
	
	// refresh des infos par le thread de traitement
	protected void updatePosition(Map map, int cellId) {
		this.pathfinding.updatePosition(map, cellId);
	}

	// refresh des infos par le thread de traitement
	protected void updatePosition(int cellId) {
		this.pathfinding.updatePosition(cellId);
	}
	
	 // changement de cellule
	protected boolean moveTo(int targetId, boolean changeMap) {
		this.character.waitState(CharacterState.IS_FREE);

		if(this.character.infos.currentCellId == targetId) { // déjà sur la cellule cible
			this.character.instance.log.p("Already on the target cell id.");
			return true;
		}
		
		this.character.instance.log.p("Moving from cell " + this.character.infos.currentCellId + " to " + targetId + ".");
		
		Vector<Integer> path = pathfinding.getCellsPathTo(targetId);
		if(path == null)
			return false;
		
		this.character.instance.log.p("Sending movement request.");
		GameMapMovementRequestMessage GMMRM = new GameMapMovementRequestMessage();
		GMMRM.serialize(path, this.character.infos.currentMap.id, this.character.instance.id);
		this.character.instance.outPush(GMMRM);
		this.character.waitState(CharacterState.CAN_MOVE); // on attend le GameMapMovementMessage
		
		int duration = this.pathfinding.getCellsPathDuration();
		this.character.instance.log.p("Movement duration : " + duration + " ms.");

		try {
			Thread.sleep(duration); // on attend d'arriver à destination
		} catch(InterruptedException e) {
			this.character.interrupt();
			return false;
		}
		
		this.character.instance.log.p("Target cell reached.");
		EmptyMessage EM = new EmptyMessage("GameMapMovementConfirmMessage");
		this.character.instance.outPush(EM);
		this.character.updateState(CharacterState.CAN_MOVE, false);
		return true;
	}
	
	// définition d'une map de destination
	protected void defineTargetMap(int mapId) {
		this.character.waitState(CharacterState.IS_LOADED); // attendre le refresh des infos
		if(this.character.infos.currentMap.id == mapId) { // déjà sur la map cible
			this.character.instance.log.p("Already on the target map.");
			return;
		}
		this.character.instance.log.p("Going from map " + this.character.infos.currentMap.id + " to map " + mapId + ".");
		boolean isInIncarnam = mapIsInIncarnam(this.character.infos.currentMap);
		if(mapIsInIncarnam(MapsCache.loadMap(mapId))) {
			if(!isInIncarnam)
				goUpToIncarnam();
		}
		else {
			if(isInIncarnam)
				goDownToAstrub();
		}
		
		this.character.waitState(CharacterState.IS_LOADED); // attendre le refresh des infos
		this.pathfinding.setTargetMap(mapId);
	}
	
	// définition d'une aire de destination et de parcours
	private void defineArea(int areaId) {
		this.character.instance.log.p("Going from map " + this.character.infos.currentMap.id + " to area " + areaId + ".");
		boolean isInIncarnam = mapIsInIncarnam(this.character.infos.currentMap);
		if(areaIsInIncarnam(areaId)) {
			if(!isInIncarnam)
				goUpToIncarnam();
		}
		else {
			if(isInIncarnam)
				goDownToAstrub();
		}
		
		this.character.waitState(CharacterState.IS_LOADED); // attendre le refresh des infos
		this.pathfinding.setArea(areaId);
	}
	
	// déplacement vers une cible fixe
	protected void goTo(int mapId) {
		defineTargetMap(mapId);
		Direction direction;
		while((direction = this.pathfinding.nextDirectionForReachTarget()) != null)
			this.changeMap(direction);
	}
	
	// déplacement vers une cible se déplaçant
	protected void dynamicGoTo(int mapId) {
		defineTargetMap(mapId);
		this.changeMap(this.pathfinding.nextDirectionForReachTarget());
	}
	
	// déplacement vers la plus proche map d'une aire
	protected void goToArea(int areaId) {
		this.character.waitState(CharacterState.IS_LOADED); // attendre le refresh des infos
		if(this.character.infos.currentMap.subareaId == areaId) { // déjà sur l'aire
			this.pathfinding.setArea(areaId); // pas besoin du calcul du chemin
			this.character.instance.log.p("Already on the target area.");
			return;
		}
		defineArea(areaId);
		Direction direction;
		while((direction = this.pathfinding.nextDirectionForReachTarget()) != null)
			this.changeMap(direction);
	}
	
	// fonction réservée aux parcours d'aires
	protected boolean changeMap() {
		return changeMap(this.pathfinding.nextDirectionInArea());
	}
	
	// fonction réservée aux chemins à destination fixe
	private boolean changeMap(Direction direction) {
		if(direction == null || !moveTo(direction.outgoingCellId, true))
			return false;
		
		this.character.instance.log.p("Moving to " + Map.directionToString(direction.direction) + " map.");
		
		int nextMapId = this.character.infos.currentMap.getNeighbourMapFromDirection(direction.direction);
		this.character.instance.log.p("Sending map changement request. Next map id : " + nextMapId + ".");
		ChangeMapMessage CMM = new ChangeMapMessage();
		CMM.serialize(nextMapId);
		this.character.instance.outPush(CMM);
		this.character.infos.mapsTravelled++;
		this.character.instance.log.graphicalFrame.setMapsTravelledCounter(this.character.infos.mapsTravelled);
			
		this.character.updateState(CharacterState.IS_LOADED, false); // chargement de map
		return true;
	}
	
	private void goDownToAstrub() {
		this.character.instance.log.p("Going down to Astrub.");
		defineTargetMap(153880835); // map où se situe le pnj
		Direction direction;
		while(!Thread.currentThread().isInterrupted() && (direction = this.pathfinding.nextDirectionForReachTarget()) != null)
			changeMap(direction);
		
		this.character.waitState(CharacterState.IS_LOADED); // important
		
		NpcGenericActionRequestMessage NGARM = new NpcGenericActionRequestMessage();
		NGARM.serialize(-10001, 3, this.character.infos.currentMap.id, this.character.instance.id); // on parle au pnj
		this.character.instance.outPush(NGARM);
		
		try {
			Thread.sleep(1000); // on attend la première question
		} catch (InterruptedException e) {
			this.character.interrupt();
			return;
		}
		
		NpcDialogReplyMessage NDRM = new NpcDialogReplyMessage();
		NDRM.serialize(25209); // on sélectionne la première réponse
		this.character.instance.outPush(NDRM);
		
		try {
			Thread.sleep(1000); // on attend la seconde question
		} catch (InterruptedException e) {
			this.character.interrupt();
			return;
		}
		
		NDRM = new NpcDialogReplyMessage();
		NDRM.serialize(25207); // on sélectionne la seconde réponse
		this.character.instance.outPush(NDRM);
		
		this.character.updateState(CharacterState.IS_LOADED, false); // chargement de map
	}
	
	private void goUpToIncarnam() {
		this.character.instance.log.p("Going up to Incarnam.");
		defineTargetMap(84674054); // map où se situe la statue Féca
		Direction direction;
		while(!Thread.currentThread().isInterrupted() && (direction = this.pathfinding.nextDirectionForReachTarget()) != null)
			changeMap(direction);
		this.character.useInteractive(375, 489378, 168278); // utilisation de la statue Féca
	}
	
	private static boolean mapIsInIncarnam(Map map) {
		if(map.subareaId >= 422 && map.subareaId <= 450)
			return true;
		return false;
	}
	
	private static boolean areaIsInIncarnam(int areaId) {
		if(areaId >= 422 && areaId <= 450)
			return true;
		return false;
	}
}