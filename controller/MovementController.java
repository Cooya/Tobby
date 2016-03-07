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
	private CharacterController CC;
	protected Pathfinding pathfinding;
	
	public MovementController(CharacterController CC) {
		this.CC = CC;
		this.pathfinding = new Pathfinding();
	}
	
	// changement de cellule
	protected boolean moveTo(int targetId, boolean changeMap) {
		this.CC.waitState(CharacterState.IS_FREE);
		if(this.CC.isInterrupted())
			return false;

		if(this.CC.infos.currentCellId == targetId) { // déjà sur la cellule cible
			this.CC.instance.log.p("Already on the target cell id.");
			return true;
		}
		
		this.CC.instance.log.p("Moving from cell " + this.CC.infos.currentCellId + " to " + targetId + ".");
		
		Vector<Integer> path = pathfinding.getCellsPathTo(targetId);
		if(path == null)
			return false;
		
		this.CC.instance.log.p("Sending movement request.");
		GameMapMovementRequestMessage GMMRM = new GameMapMovementRequestMessage();
		GMMRM.serialize(path, this.CC.infos.currentMap.id, this.CC.instance.id);
		this.CC.instance.outPush(GMMRM);
		this.CC.updateState(CharacterState.IN_MOVEMENT, true);
		this.CC.waitState(CharacterState.CAN_MOVE); // on attend le GameMapMovementMessage
		
		int duration = this.pathfinding.getCellsPathDuration();
		this.CC.instance.log.p("Movement duration : " + duration + " ms.");

		try {
			Thread.sleep(duration); // on attend d'arriver à destination
		} catch(InterruptedException e) {
			this.CC.interrupt();
			return false;
		}
		
		this.CC.instance.log.p("Target cell reached.");
		EmptyMessage EM = new EmptyMessage("GameMapMovementConfirmMessage");
		this.CC.instance.outPush(EM);
		this.CC.updateState(CharacterState.CAN_MOVE, false);
		this.CC.updateState(CharacterState.IN_MOVEMENT, false);
		return true;
	}
	
	// changement de map (map distante)
	protected void goTo(int mapId) {
		this.CC.waitState(CharacterState.IS_FREE);
		if(this.CC.isInterrupted())
			return;
		
		if(this.CC.infos.currentMap.id == mapId) { // déjà sur la map cible
			this.CC.instance.log.p("Already on the target map id.");
			return;
		}
		
		this.CC.instance.log.p("Going from map " + this.CC.infos.currentMap.id + " to " + mapId + ".");
		
		boolean isInIncarnam = mapIsInIncarnam(this.CC.infos.currentMap);
		boolean targetIsInIncarnam = mapIsInIncarnam(MapsCache.loadMap(mapId));
		if(targetIsInIncarnam) {
			if(!isInIncarnam)
				goUpToIncarnam();
		}
		else {
			if(isInIncarnam)
				goDownToAstrub();
		}
		
		if(this.CC.isInterrupted())
			return;
		
		travel(this.pathfinding.pathToMap(mapId));
		
		this.CC.instance.log.p("Map cell reached.");
	}
	
	protected void goToArea(int areaId) {
		if(this.CC.infos.currentMap.subareaId == areaId) // déjà sur l'aire
			return;
		boolean isInIncarnam = mapIsInIncarnam(this.CC.infos.currentMap);
		if(areaIsInIncarnam(areaId)) {
			if(!isInIncarnam)
				this.CC.mvt.goUpToIncarnam();
		}
		else {
			if(isInIncarnam)
				this.CC.mvt.goDownToAstrub();
		}
		if(Thread.interrupted())
			return;
		
		this.CC.mvt.travel(this.CC.mvt.pathfinding.pathToArea());
	}
	
	// changement de map (map collatéral)
	public boolean changeMap(Direction direction) {
		this.CC.waitState(CharacterState.IS_FREE);
		if(this.CC.isInterrupted())
			return false;

		this.CC.instance.log.p("Moving to " + Map.directionToString(direction.direction) + " map.");

		if(!moveTo(direction.outgoingCellId, true))
			return false;
		
		if(this.CC.isInterrupted())
			return false;
		
		int nextMapId = this.CC.infos.currentMap.getNeighbourMapFromDirection(direction.direction);
		this.CC.instance.log.p("Sending map changement request. Next map id : " + nextMapId + ".");
		ChangeMapMessage CMM = new ChangeMapMessage();
		CMM.serialize(nextMapId);
		this.CC.instance.outPush(CMM);
		this.CC.infos.mapsTravelled++;
		this.CC.instance.log.graphicalFrame.setMapsTravelledCounter(this.CC.infos.mapsTravelled);
			
		this.CC.updateState(CharacterState.IS_LOADED, false);
		return true;
	}
	
	public boolean changeMap() {
		return changeMap(this.pathfinding.nextDirection());
	}
	
	private void goDownToAstrub() {
		this.CC.waitState(CharacterState.IS_FREE);
		if(this.CC.isInterrupted())
			return;
		
		this.CC.instance.log.p("Going down to Astrub.");
		travel(this.pathfinding.pathToMap(153880835)); // map où se situe le pnj
		if(Thread.interrupted())
			return;
		
		NpcGenericActionRequestMessage NGARM = new NpcGenericActionRequestMessage();
		NGARM.serialize(-10001, 3, this.CC.infos.currentMap.id, this.CC.instance.id); // on parle au pnj
		this.CC.instance.outPush(NGARM);
		
		try {
			Thread.sleep(1000); // on attend la première question
		} catch (InterruptedException e) {
			this.CC.interrupt();
			return;
		}
		
		NpcDialogReplyMessage NDRM = new NpcDialogReplyMessage();
		NDRM.serialize(25209); // on sélectionne la première réponse
		this.CC.instance.outPush(NDRM);
		
		try {
			Thread.sleep(1000); // on attend la seconde question
		} catch (InterruptedException e) {
			this.CC.interrupt();
			return;
		}
		
		NDRM = new NpcDialogReplyMessage();
		NDRM.serialize(25207); // on sélectionne la seconde réponse
		this.CC.instance.outPush(NDRM);
		
		this.CC.updateState(CharacterState.IS_LOADED, false);
	}
	
	private void goUpToIncarnam() {
		this.CC.waitState(CharacterState.IS_FREE);
		this.CC.instance.log.p("Going up to Incarnam.");
		travel(this.pathfinding.pathToMap(84674054));
		this.CC.useInteractive(375, 489378, 168278); // utilisation de la statue Féca
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