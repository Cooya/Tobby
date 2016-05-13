package controller.api;

import java.util.Vector;

import controller.CharacterState;
import controller.characters.Character;
import controller.pathfinding.Pathfinding;
import controller.pathfinding.Pathfinding.Direction;
import gamedata.d2p.MapsCache;
import gamedata.d2p.ankama.Map;
import messages.UnhandledMessage;
import messages.context.ChangeMapMessage;
import messages.context.GameMapMovementRequestMessage;
import messages.interactions.NpcDialogReplyMessage;
import messages.interactions.NpcGenericActionRequestMessage;

public class MovementAPI {
	private Character character;
	private Pathfinding pathfinding;
	
	public MovementAPI(Character character) {
		this.character = character;
		this.pathfinding = new Pathfinding();
	}
	
	// refresh des infos par le thread de traitement
	public void updatePosition(Map map, int cellId) {
		this.pathfinding.updatePosition(map, cellId);
	}

	// refresh des infos par le thread de traitement
	public void updatePosition(int cellId) {
		this.pathfinding.updatePosition(cellId);
	}
	
	 // changement de cellule
	public boolean moveTo(int targetId) {
		this.character.waitState(CharacterState.IS_FREE);
		
		int currentCellId = this.character.infos.getCurrentCellId();
		if(currentCellId == targetId) { // déjà sur la cellule cible
			this.character.log.p("Already on the target cell id.");
			return true;
		}
		
		this.character.log.p("Moving from cell " + currentCellId + " to " + targetId + ".");
		
		Vector<Integer> path = pathfinding.getCellsPathTo(targetId);
		if(path == null)
			return false;
		
		this.character.log.p("Sending movement request.");
		GameMapMovementRequestMessage GMMRM = new GameMapMovementRequestMessage();
		GMMRM.keyMovements = path;
		GMMRM.mapId = this.character.infos.getCurrentMap().id;
		this.character.net.send(GMMRM);
		this.character.waitState(CharacterState.CAN_MOVE); // on attend le GameMapMovementMessage
		
		int duration = this.pathfinding.getCellsPathDuration();
		this.character.log.p("Movement duration : " + duration + " ms.");

		try {
			Thread.sleep(duration); // on attend d'arriver à destination
		} catch(InterruptedException e) {
			this.character.interrupt();
			return false;
		}
		
		this.character.log.p("Target cell reached.");
		this.character.net.send(new UnhandledMessage("GameMapMovementConfirmMessage"));
		this.character.updateState(CharacterState.CAN_MOVE, false);
		return true;
	}
	
	// définition d'une map de destination
	public void defineTargetMap(int mapId) {
		Map currentMap = this.character.infos.getCurrentMap();
		this.character.waitState(CharacterState.IS_LOADED); // attendre le refresh des infos
		if(currentMap.id == mapId) { // déjà sur la map cible
			this.character.log.p("Already on the target map.");
			return;
		}
		if(mapIsInCelestialTemple(currentMap))
			goOutFromCelestialTemple();
		this.character.log.p("Going from map " + currentMap.id + " to map " + mapId + ".");
		boolean isInIncarnam = mapIsInIncarnam(currentMap);
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
	public void defineArea(int areaId) {
		Map currentMap = this.character.infos.getCurrentMap();
		if(mapIsInCelestialTemple(currentMap))
			goOutFromCelestialTemple();
		this.character.waitState(CharacterState.IS_LOADED); // attendre le refresh des infos
		this.character.log.p("Going from map " + currentMap.id + " to area " + areaId + ".");
		boolean isInIncarnam = mapIsInIncarnam(currentMap);
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
	public void goTo(int mapId) {
		defineTargetMap(mapId);
		Direction direction;
		while((direction = this.pathfinding.nextDirectionForReachTarget()) != null)
			this.changeMap(direction);
	}
	
	// déplacement vers une cible se déplaçant
	public void dynamicGoTo(int mapId) {
		defineTargetMap(mapId);
		this.changeMap(this.pathfinding.nextDirectionForReachTarget());
	}
	
	// déplacement vers la plus proche map d'une aire
	public void goToArea(int areaId) {
		if(!this.character.waitState(CharacterState.IS_LOADED)) // attendre le refresh des infos
			return; // en cas d'interruption (c'est déjà arrivé plusieurs fois)
		if(this.character.infos.getCurrentMap().subareaId == areaId) { // déjà sur l'aire
			this.pathfinding.setArea(areaId); // pas besoin du calcul du chemin
			this.character.log.p("Already on the target area.");
			return;
		}
		defineArea(areaId);
		Direction direction;
		while((direction = this.pathfinding.nextDirectionForReachTarget()) != null)
			this.changeMap(direction);
	}
	
	// fonction réservée aux parcours d'aires
	public boolean changeMap() {
		return changeMap(this.pathfinding.nextDirectionInArea());
	}
	
	// fonction réservée aux chemins à destination fixe
	public boolean changeMap(Direction direction) {
		if(direction == null || !moveTo(direction.outgoingCellId))
			return false;
		
		this.character.log.p("Moving to " + Map.directionToString(direction.direction) + " map.");
		
		int nextMapId = this.character.infos.getCurrentMap().getNeighbourMapFromDirection(direction.direction);
		this.character.log.p("Sending map changement request. Next map id : " + nextMapId + ".");
		ChangeMapMessage CMM = new ChangeMapMessage();
		CMM.mapId = nextMapId;
		this.character.net.send(CMM);
		this.character.infos.incMapsTravelledCounter();
			
		this.character.updateState(CharacterState.IS_LOADED, false); // chargement de map
		return true;
	}
	
	// fait descendre le personnage à Astrub depuis Incarnam
	private void goDownToAstrub() {
		this.character.log.p("Going down to Astrub.");
		defineTargetMap(153880835); // map où se situe le pnj
		Direction direction;
		while(!Thread.currentThread().isInterrupted() && (direction = this.pathfinding.nextDirectionForReachTarget()) != null)
			changeMap(direction);
		
		this.character.waitState(CharacterState.IS_LOADED); // important
		
		// on parle au pnj
		NpcGenericActionRequestMessage NGARM = new NpcGenericActionRequestMessage();
		NGARM.npcId = (int) this.character.roleplayContext.getNpcContextualId(2889);
		NGARM.npcActionId = 3;
		NGARM.npcMapId = this.character.infos.getCurrentMap().id;
		this.character.net.send(NGARM);
		
		try {
			Thread.sleep(1000); // on attend la première question
		} catch (InterruptedException e) {
			this.character.interrupt();
			return;
		}
		
		NpcDialogReplyMessage NDRM = new NpcDialogReplyMessage();
		NDRM.replyId = 25209; // on sélectionne la première réponse
		this.character.net.send(NDRM);
		
		try {
			Thread.sleep(1000); // on attend la seconde question
		} catch (InterruptedException e) {
			this.character.interrupt();
			return;
		}
		
		NDRM = new NpcDialogReplyMessage();
		NDRM.replyId = 25207; // on sélectionne la seconde réponse
		this.character.net.send(NDRM);
		
		this.character.updateState(CharacterState.IS_LOADED, false); // chargement de map
	}
	
	// fait monter le personnage à Incarnam depuis Astrub
	private void goUpToIncarnam() {
		this.character.log.p("Going up to Incarnam.");
		defineTargetMap(84674054); // map où se situe la statue Féca
		Direction direction;
		while(!Thread.currentThread().isInterrupted() && (direction = this.pathfinding.nextDirectionForReachTarget()) != null)
			changeMap(direction);
		this.character.interaction.useInteractive(375, 489378, true); // utilisation de la statue Féca
	}
	
	// fait sortir le personnage du temple céleste d'Incarnam
	private void goOutFromCelestialTemple() {
		this.character.log.p("Going out from celestial temple of Incarnam.");
		if(this.character.infos.getCurrentMap().id == 153092354) // première salle
			this.character.interaction.useInteractive(396, 489318, true); // escalier
		changeMap(new Direction(Map.RIGHT, 531)); // on sort du temple
	}
	
	// vérifie si la map donnée est à Incarnam ou non
	private static boolean mapIsInIncarnam(Map map) {
		return (map.subareaId >= 422 && map.subareaId <= 450);
	}
	
	// vérifie si la map donnée est dans le temple céleste ou non
	private static boolean mapIsInCelestialTemple(Map map) {
		return map.id == 153092354 || map.id == 152043521;
	}
	
	// vérifie si l'aire donnée est à Incarnam ou non 
	private static boolean areaIsInIncarnam(int areaId) {
		return (areaId >= 422 && areaId <= 450);
	}
}