package controller;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import gamedata.d2p.MapsCache;
import gamedata.d2p.ankama.Map;
import gamedata.d2p.ankama.MapPoint;
import gamedata.d2p.ankama.MovementPath;
import main.Instance;
import messages.EmptyMessage;
import messages.context.ChangeMapMessage;
import messages.context.GameMapMovementRequestMessage;
import messages.interactions.InteractiveUseRequestMessage;
import messages.interactions.NpcDialogReplyMessage;
import messages.interactions.NpcGenericActionRequestMessage;
import controller.pathfinding.CellsPathfinder;
import controller.pathfinding.Path;
import controller.pathfinding.Pathfinder;
import controller.pathfinding.PathsCache;

public class MovementController {
	private CharacterController CC;
	private Path currentPath;
	protected CellsPathfinder pathfinder;
	
	public MovementController(CharacterController CC) {
		this.CC = CC;
	}
	
	protected void moveTo(int cellId, boolean changeMap) {
		this.CC.waitState(CharacterState.IS_FREE);
		if(this.CC.isInterrupted())
			return;

		if(this.CC.infos.currentCellId == cellId) { // déjà sur la cellule cible
			this.CC.instance.log.p("Already on the target cell id.");
			return;
		}
		
		this.CC.instance.log.p("Moving from cell " + this.CC.infos.currentCellId + " to " + cellId + ".");

		this.pathfinder = new CellsPathfinder(this.CC.infos.currentMap);
		this.currentPath = this.pathfinder.compute(this.CC.infos.currentCellId, cellId);
		
		this.CC.instance.log.p(this.currentPath.toString());
		
		MovementPath mvPath = CellsPathfinder.movementPathFromArray(this.currentPath.toVector());
		mvPath.setStart(MapPoint.fromCellId(this.CC.infos.currentCellId));
		mvPath.setEnd(MapPoint.fromCellId(cellId));
		
		this.CC.instance.log.p("Sending movement request.");
	
		GameMapMovementRequestMessage GMMRM = new GameMapMovementRequestMessage();
		GMMRM.serialize(mvPath.getServerMovement(), this.CC.infos.currentMap.id, this.CC.instance.id);
		this.CC.instance.outPush(GMMRM);
		this.CC.updateState(CharacterState.IN_MOVEMENT, true);
		
		int duration = this.currentPath.getCrossingDuration();
		this.CC.instance.log.p("Movement duration : " + duration + " ms.");

		try {
			Thread.sleep(duration); // on attend d'arriver à destination
		} catch(InterruptedException e) {
			this.CC.interrupt();
			return;
		}
		
		this.CC.instance.log.p("Target cell reached.");

		EmptyMessage EM = new EmptyMessage("GameMapMovementConfirmMessage");
		this.CC.instance.outPush(EM);
		this.CC.updateState(CharacterState.IN_MOVEMENT, false);
		this.CC.waitState(CharacterState.IS_FREE);
	}
	
	protected void goTo(int mapId) {
		this.CC.waitState(CharacterState.IS_FREE);
		if(this.CC.isInterrupted())
			return;
		
		if(this.CC.infos.currentMap.id == mapId) { // déjà sur la cellule cible
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
		
		PathsCache.moveTo(mapId, this.CC);
		
		this.CC.instance.log.p("Map cell reached.");
	}

	public void changeMap(int direction) {
		this.CC.waitState(CharacterState.IS_FREE);
		if(this.CC.isInterrupted())
			return;

		this.CC.instance.log.p("Moving to " + Pathfinder.directionToString(direction) + " map.");

		moveTo(this.pathfinder.getChangementMapCell(direction), true);
		
		if(this.CC.isInterrupted())
			return;
		
		int nextMapId = this.CC.infos.currentMap.getNeighbourMapFromDirection(direction);
		this.CC.instance.log.p("Sending map changement request. Next map id : " + nextMapId + ".");
		ChangeMapMessage CMM = new ChangeMapMessage();
		CMM.serialize(nextMapId);
		this.CC.instance.outPush(CMM);
			
		this.CC.updateState(CharacterState.IS_LOADED, false);
		this.CC.waitState(CharacterState.IS_FREE);  // on attend la fin du changement de map
	}
	
	private void goDownToAstrub() {
		this.CC.waitState(CharacterState.IS_FREE);
		if(this.CC.isInterrupted())
			return;
		
		this.CC.instance.log.p("Going down to Astrub.");
		PathsCache.moveTo(153880835, this.CC); // map où se situe le pnj
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
		this.CC.waitState(CharacterState.IS_FREE);
	}
	
	private void goUpToIncarnam() {
		this.CC.waitState(CharacterState.IS_FREE);
		if(this.CC.isInterrupted())
			return;
		
		this.CC.instance.log.p("Going up to Incarnam.");
		PathsCache.moveTo(84674054, this.CC);
		if(this.CC.isInterrupted())
			return;
		
		moveTo(375, false); // entrée de la statue Féca
		if(this.CC.isInterrupted())
			return;
		
		InteractiveUseRequestMessage IURM = new InteractiveUseRequestMessage();
		IURM.serialize(489378, 168278, this.CC.instance.id); // utilisation de la statue Féca
		this.CC.instance.outPush(IURM);
		
		this.CC.updateState(CharacterState.IS_LOADED, false);
		this.CC.waitState(CharacterState.IS_FREE);
	}
	
	private static boolean mapIsInIncarnam(Map map) {
		if(map.subareaId >= 422 && map.subareaId <= 450)
			return true;
		return false;
	}

	protected static class AreaRover {
		private CharacterController CC;
		private int areaId;
		
		protected AreaRover(int areaId, CharacterController CC) {
			this.CC = CC;
			this.areaId = areaId;
			goToAreaIfRequired();
		}
	
		protected int nextMap() {
			goToAreaIfRequired();
			if(Thread.interrupted())
				return -1;
			
			
			Hashtable<Integer, Integer> neighbours  = new Hashtable<Integer, Integer>();
			for(int direction = 0; direction < 8; direction += 2)
				neighbours.put(direction, this.CC.infos.currentMap.getNeighbourMapFromDirection(direction));
			
			Random randomGen = new Random();
			int randomDirection;
			Map map;
			while(true) {
				List<Integer> directionList = Collections.list(neighbours.keys());
				randomDirection = directionList.get(randomGen.nextInt(neighbours.size())); // on prend une direction au hasard
				map = MapsCache.loadMap(neighbours.get(randomDirection));
				if(map != null && map.subareaId == this.CC.infos.currentMap.subareaId)
					return randomDirection;
				else {
					Instance.log("Direction to " + Pathfinder.directionToString(randomDirection) + " impossible.");
					neighbours.remove(randomDirection);
				}
			}
		}
		
		private static boolean areaIsInIncarnam(int areaId) {
			if(areaId >= 422 && areaId <= 450)
				return true;
			return false;
		}
		
		private void goToAreaIfRequired() {
			boolean isInIncarnam = mapIsInIncarnam(this.CC.infos.currentMap);
			if(areaIsInIncarnam(this.areaId)) {
				if(!isInIncarnam)
					this.CC.mvt.goUpToIncarnam();
			}
			else {
				if(isInIncarnam)
					this.CC.mvt.goDownToAstrub();
			}
			
			if(Thread.interrupted())
				return;
			
			if(this.CC.infos.currentMap.subareaId != this.areaId) {
				Instance.log("Going to area with id = " + this.areaId + ".");
				Path.getPathToArea(areaId, this.CC.infos).run(this.CC);
			}
		}
	}
}