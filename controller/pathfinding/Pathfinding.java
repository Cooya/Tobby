package controller.pathfinding;

import gamedata.d2p.MapsCache;
import gamedata.d2p.ankama.Map;
import gamedata.d2p.ankama.MapPoint;
import gamedata.d2p.ankama.MovementPath;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import main.FatalError;
import controller.pathfinding.Path.Direction;

// classe interface qui fait le lien entre le package "pathfinding" et les contrôleurs,
// elle ne retourne que des chemins ou des directions

public class Pathfinding {
	private LightMapNode mapNode;
	private int currentCellId;
	private Path currentCellsPath;
	private int areaId;
	private int lastDirection;
	
	public void updatePosition(Map map, int currentCellId) {
		this.mapNode = new LightMapNode(map);
		this.currentCellId = currentCellId;
	}
	
	public void updatePosition(int currentCellId) {
		this.currentCellId = currentCellId;
	}
	
	public void updateArea(int areaId) {
		this.areaId = areaId;
	}
	
	public Vector<Integer> getCellsPathTo(int targetId) {
		Pathfinder pathfinder = new CellsPathfinder(this.mapNode.map);
		this.currentCellsPath = pathfinder.compute(this.currentCellId, targetId);
		if(this.currentCellsPath == null)
			return null;
		MovementPath mvPath = CellsPathfinder.movementPathFromArray(this.currentCellsPath.toVector());
		mvPath.setStart(MapPoint.fromCellId(this.currentCellId));
		mvPath.setEnd(MapPoint.fromCellId(targetId));
		return mvPath.getServerMovement();
	}
	
	public int getCellsPathDuration() {
		return this.currentCellsPath.getCrossingDuration();
	}
	
	public Path pathToMap(int mapId) {
		return PathsCache.toMap(mapId, this.mapNode.map.id, this.currentCellId);
	}
	
	public Path pathToArea() {
		return PathsCache.toArea(this.areaId, this.mapNode.map.id, this.currentCellId);
	}

	public Direction nextDirection() {
		if(mapNode.map.subareaId != this.areaId)
			throw new FatalError("Bad current area.");
		
		Hashtable<Integer, Integer> neighbours  = new Hashtable<Integer, Integer>();
		for(int direction = 0; direction < 8; direction += 2)
			neighbours.put(direction, mapNode.map.getNeighbourMapFromDirection(direction));
		
		// priorité à la direction opposée
		Map map = MapsCache.loadMap(neighbours.get(this.lastDirection));
		if(map != null && map.mapType == 0 && map.subareaId == mapNode.map.subareaId) {
			int mapChangementCell = mapNode.getMapChangementCell(this.lastDirection);
			if(mapChangementCell != -1)
				return new Direction(this.lastDirection, mapChangementCell);
		}
		
		int incomingDirection = getOppositeDirection(this.lastDirection);
		neighbours.remove(this.lastDirection); // on ne peut pas aller à la direction opposée
		neighbours.remove(incomingDirection); // pour éviter les retours en arrière
		
		Random randomGen = new Random();
		int randomDirection;
		while(neighbours.size() > 0) {
			randomDirection = Collections.list(neighbours.keys()).get(randomGen.nextInt(neighbours.size())); // on prend une direction au hasard
			map = MapsCache.loadMap(neighbours.get(randomDirection));
			if(map != null && map.mapType == 0 && map.subareaId == mapNode.map.subareaId) {
				this.lastDirection = randomDirection;
				int mapChangementCell = mapNode.getMapChangementCell(this.lastDirection);
				if(mapChangementCell != -1)
					return new Direction(this.lastDirection, mapChangementCell);
					
			}
			neighbours.remove(randomDirection);
		}
		this.lastDirection = incomingDirection;
		return new Direction(this.lastDirection, mapNode.getMapChangementCell(this.lastDirection));
	}
	
	private int getOppositeDirection(int direction) {
		if(direction >= 4)
			return direction - 4;
		else
			return direction + 4;
	}
}