package controller;

import java.util.Vector;

import controller.pathfinding.Path;
import controller.pathfinding.Path.Direction;
import controller.pathfinding.Pathfinding;
import gamedata.d2p.MapsCache;
import gamedata.d2p.ankama.Map;
import messages.EmptyMessage;
import messages.context.ChangeMapMessage;
import messages.context.GameMapMovementRequestMessage;
import messages.interactions.NpcDialogReplyMessage;
import messages.interactions.NpcGenericActionRequestMessage;

public class MovementController {
	private CharacterController character;
	protected Pathfinding pathfinding;
	
	public MovementController(CharacterController character) {
		this.character = character;
		this.pathfinding = new Pathfinding();
	}
	
	// changement de cellule
	protected boolean moveTo(int targetId, boolean changeMap) {
		this.character.waitState(CharacterState.IS_FREE);
		if(this.character.isInterrupted())
			return false;

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
	
	// changement de map (map distante)
	protected void goTo(int mapId) {
		this.character.waitState(CharacterState.IS_LOADED);
		if(this.character.isInterrupted())
			return;
		
		if(this.character.infos.currentMap.id == mapId) { // déjà sur la map cible
			this.character.instance.log.p("Already on the target map id.");
			return;
		}
		
		this.character.instance.log.p("Going from map " + this.character.infos.currentMap.id + " to " + mapId + ".");
		
		boolean isInIncarnam = mapIsInIncarnam(this.character.infos.currentMap);
		boolean targetIsInIncarnam = mapIsInIncarnam(MapsCache.loadMap(mapId));
		if(targetIsInIncarnam) {
			if(!isInIncarnam)
				goUpToIncarnam();
		}
		else {
			if(isInIncarnam)
				goDownToAstrub();
		}
		
		if(this.character.isInterrupted())
			return;
		this.character.waitState(CharacterState.IS_LOADED);
		travel(this.pathfinding.pathToMap(mapId));
		this.character.instance.log.p("Map cell reached.");
	}
	
	protected void goToArea(int areaId) {
		if(this.character.infos.currentMap.subareaId == areaId) // déjà sur l'aire
			return;
		boolean isInIncarnam = mapIsInIncarnam(this.character.infos.currentMap);
		if(areaIsInIncarnam(areaId)) {
			if(!isInIncarnam)
				goUpToIncarnam();
		}
		else {
			if(isInIncarnam)
				goDownToAstrub();
		}
		if(Thread.interrupted())
			return;
		
		this.character.waitState(CharacterState.IS_LOADED);
		this.character.mvt.travel(this.character.mvt.pathfinding.pathToArea());
	}
	
	// changement de map (map collatéral)
	public boolean changeMap(Direction direction) {
		this.character.waitState(CharacterState.IS_LOADED);
		if(this.character.isInterrupted())
			return false;

		this.character.instance.log.p("Moving to " + Map.directionToString(direction.direction) + " map.");

		if(!moveTo(direction.outgoingCellId, true))
			return false;
		
		if(this.character.isInterrupted())
			return false;
		
		int nextMapId = this.character.infos.currentMap.getNeighbourMapFromDirection(direction.direction);
		this.character.instance.log.p("Sending map changement request. Next map id : " + nextMapId + ".");
		ChangeMapMessage CMM = new ChangeMapMessage();
		CMM.serialize(nextMapId);
		this.character.instance.outPush(CMM);
		this.character.infos.mapsTravelled++;
		this.character.instance.log.graphicalFrame.setMapsTravelledCounter(this.character.infos.mapsTravelled);
			
		this.character.updateState(CharacterState.IS_LOADED, false);
		return true;
	}
	
	public boolean changeMap() {
		this.character.waitState(CharacterState.IS_LOADED);
		return changeMap(this.pathfinding.nextDirection());
	}
	
	private void goDownToAstrub() {
		this.character.waitState(CharacterState.IS_LOADED);
		if(this.character.isInterrupted())
			return;
		
		this.character.instance.log.p("Going down to Astrub.");
		travel(this.pathfinding.pathToMap(153880835)); // map où se situe le pnj
		if(Thread.interrupted())
			return;
		
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
		
		this.character.updateState(CharacterState.IS_LOADED, false);
	}
	
	private void goUpToIncarnam() {
		this.character.waitState(CharacterState.IS_LOADED);
		this.character.instance.log.p("Going up to Incarnam.");
		travel(this.pathfinding.pathToMap(84674054));
		this.character.useInteractive(375, 489378, 168278); // utilisation de la statue Féca
	}
	
	private void travel(Path path) {
		Direction direction;
		while(!Thread.currentThread().isInterrupted() && (direction = path.nextDirection()) != null)
			changeMap(direction);
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