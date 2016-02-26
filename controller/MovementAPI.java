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

public class MovementAPI {
	
	protected static void moveTo(int cellId, boolean changeMap, CharacterController CC) {
		CC.waitState(CharacterState.IS_FREE);
		if(CC.isInterrupted())
			return;

		if(CC.infos.currentCellId == cellId) { // déjà sur la cellule cible
			CC.instance.log.p("Already on the target cell id.");
			return;
		}
		
		CC.instance.log.p("Moving from cell " + CC.infos.currentCellId + " to " + cellId + ".");

		CC.pathfinder = new CellsPathfinder(CC.infos.currentMap);
		Path path = CC.pathfinder.compute(CC.infos.currentCellId, cellId);
		
		CC.instance.log.p(path.toString());
		
		MovementPath mvPath = CellsPathfinder.movementPathFromArray(path.toVector());
		mvPath.setStart(MapPoint.fromCellId(CC.infos.currentCellId));
		mvPath.setEnd(MapPoint.fromCellId(cellId));
		
		CC.instance.log.p("Sending movement request.");
	
		GameMapMovementRequestMessage GMMRM = new GameMapMovementRequestMessage();
		GMMRM.serialize(mvPath.getServerMovement(), CC.infos.currentMap.id, CC.instance.id);
		CC.instance.outPush(GMMRM);
		CC.states.put(CharacterState.IN_MOVEMENT, true);
		
		int duration = path.getCrossingDuration();
		CC.instance.log.p("Movement duration : " + duration + " ms.");

		try {
			Thread.sleep((long) (duration * 2)); // on attend d'arriver à destination
		} catch(InterruptedException e) {
			CC.interrupt();
			return;
		}
		
		CC.instance.log.p("Target cell reached.");

		EmptyMessage EM = new EmptyMessage("GameMapMovementConfirmMessage");
		CC.instance.outPush(EM);
		CC.states.put(CharacterState.IN_MOVEMENT, false);
		CC.waitState(CharacterState.IS_FREE);
	}
	
	protected static void goTo(int mapId, CharacterController CC) {
		CC.waitState(CharacterState.IS_FREE);
		if(CC.isInterrupted())
			return;
		
		if(CC.infos.currentMap.id == mapId) { // déjà sur la cellule cible
			CC.instance.log.p("Already on the target map id.");
			return;
		}
		
		CC.instance.log.p("Going from map " + CC.infos.currentMap.id + " to " + mapId + ".");
		
		boolean isInIncarnam = mapIsInIncarnam(CC.infos.currentMap);
		boolean targetIsInIncarnam = mapIsInIncarnam(MapsCache.loadMap(mapId));
		if(targetIsInIncarnam) {
			if(!isInIncarnam)
				goUpToIncarnam(CC);
		}
		else {
			if(isInIncarnam)
				goDownToAstrub(CC);
		}
		
		if(CC.isInterrupted())
			return;
		
		PathsCache.moveTo(mapId, CC);
		
		CC.instance.log.p("Map cell reached.");
	}

	public static void changeMap(int direction, CharacterController CC) {
		CC.waitState(CharacterState.IS_FREE);
		if(CC.isInterrupted())
			return;

		CC.instance.log.p("Moving to " + Pathfinder.directionToString(direction) + " map.");

		moveTo(CC.pathfinder.getChangementMapCell(direction), true, CC);
		
		if(CC.isInterrupted())
			return;
		
		int nextMapId = CC.infos.currentMap.getNeighbourMapFromDirection(direction);
		CC.instance.log.p("Sending map changement request. Next map id : " + nextMapId + ".");
		ChangeMapMessage CMM = new ChangeMapMessage();
		CMM.serialize(nextMapId);
		CC.instance.outPush(CMM);
			
		CC.states.put(CharacterState.IS_LOADED, false);
		CC.waitState(CharacterState.IS_FREE);  // on attend la fin du changement de map
	}
	
	private static boolean mapIsInIncarnam(Map map) {
		if(map.subareaId >= 422 && map.subareaId <= 450)
			return true;
		return false;
	}
	
	private static void goDownToAstrub(CharacterController CC) {
		CC.waitState(CharacterState.IS_FREE);
		if(CC.isInterrupted())
			return;
		
		CC.instance.log.p("Going down to Astrub.");
		PathsCache.moveTo(153880835, CC); // map où se situe le pnj
		if(Thread.interrupted())
			return;
		
		NpcGenericActionRequestMessage NGARM = new NpcGenericActionRequestMessage();
		NGARM.serialize(-10001, 3, CC.infos.currentMap.id, CC.instance.id); // on parle au pnj
		CC.instance.outPush(NGARM);
		
		try {
			Thread.sleep(1000); // on attend la première question
		} catch (InterruptedException e) {
			CC.interrupt();
			return;
		}
		
		NpcDialogReplyMessage NDRM = new NpcDialogReplyMessage();
		NDRM.serialize(25209); // on sélectionne la première réponse
		CC.instance.outPush(NDRM);
		
		try {
			Thread.sleep(1000); // on attend la seconde question
		} catch (InterruptedException e) {
			CC.interrupt();
			return;
		}
		
		NDRM = new NpcDialogReplyMessage();
		NDRM.serialize(25207); // on sélectionne la seconde réponse
		CC.instance.outPush(NDRM);
		
		CC.states.put(CharacterState.IS_LOADED, false);
		CC.waitState(CharacterState.IS_FREE);
	}
	
	private static void goUpToIncarnam(CharacterController CC) {
		CC.waitState(CharacterState.IS_FREE);
		if(CC.isInterrupted())
			return;
		
		CC.instance.log.p("Going up to Incarnam.");
		PathsCache.moveTo(84674054, CC);
		if(CC.isInterrupted())
			return;
		
		moveTo(375, false, CC); // entrée de la statue Féca
		if(CC.isInterrupted())
			return;
		
		InteractiveUseRequestMessage IURM = new InteractiveUseRequestMessage();
		IURM.serialize(489378, 168278, CC.instance.id); // utilisation de la statue Féca
		CC.instance.outPush(IURM);
		
		CC.states.put(CharacterState.IS_LOADED, false);
		CC.waitState(CharacterState.IS_FREE);
	}

	protected static class AreaRover {
		private int areaId;
		
		protected AreaRover(int areaId, CharacterController CC) {
			this.areaId = areaId;
			goToAreaIfRequired(CC);
		}
	
		protected int nextMap(CharacterController CC) {
			goToAreaIfRequired(CC);
			if(Thread.interrupted())
				return -1;
			
			
			Hashtable<Integer, Integer> neighbours  = new Hashtable<Integer, Integer>();
			for(int direction = 0; direction < 8; direction += 2)
				neighbours.put(direction, CC.infos.currentMap.getNeighbourMapFromDirection(direction));
			
			Random randomGen = new Random();
			int randomDirection;
			Map map;
			while(true) {
				List<Integer> directionList = Collections.list(neighbours.keys());
				randomDirection = directionList.get(randomGen.nextInt(neighbours.size())); // on prend une direction au hasard
				map = MapsCache.loadMap(neighbours.get(randomDirection));
				if(map != null && map.subareaId == CC.infos.currentMap.subareaId)
					return randomDirection;
				else {
					Instance.log("Direction to " + Pathfinder.directionToString(randomDirection) + " impossible.");
					neighbours.remove(randomDirection);
				}
			}
		}
		
		private boolean areaIsInIncarnam(int areaId) {
			if(areaId >= 422 && areaId <= 450)
				return true;
			return false;
		}
		
		private void goToAreaIfRequired(CharacterController CC) {
			boolean isInIncarnam = mapIsInIncarnam(CC.infos.currentMap);
			if(areaIsInIncarnam(this.areaId)) {
				if(!isInIncarnam)
					goUpToIncarnam(CC);
			}
			else {
				if(isInIncarnam)
					goDownToAstrub(CC);
			}
			
			if(Thread.interrupted())
				return;
			
			if(CC.infos.currentMap.subareaId != this.areaId) {
				Instance.log("Going to area with id = " + this.areaId + ".");
				Path.getPathToArea(areaId, CC.infos).run(CC);
			}
		}
	}
}