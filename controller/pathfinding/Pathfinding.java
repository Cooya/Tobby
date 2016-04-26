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

// classe interface qui fait le lien entre le package "pathfinding" et les contrôleur de mouvement
// elle retourne soit :
// un chemin de cellules pour les changements de cases
// une durée en millisecondes d'un chemin de celulles
// une direction pour un changement de map

public class Pathfinding {
	private LightMapNode mapNode;
	private int currentCellId;
	private Path currentCellsPath;
	private Path currentMapsPath;
	private int areaId;
	private int lastDirection;
	
	public Pathfinding() {
		this.currentCellId = -1;
		this.lastDirection = -1;
		this.areaId = -1;
	}
	
	// met à jour la position du personnage
	public void updatePosition(Map map, int currentCellId) {
		this.mapNode = new LightMapNode(map, currentCellId);
		this.currentCellId = currentCellId;
	}
	
	// met à jour la position du personnage
	public void updatePosition(int currentCellId) {
		this.currentCellId = currentCellId;
	}
	
	// modifie l'aire cible et calcule si nécessaire un chemin vers cette aire
	public void setArea(int areaId) {
		this.areaId = areaId;
		if(this.mapNode.map.subareaId != areaId)
			this.currentMapsPath = PathsCache.toArea(this.areaId, this.mapNode.map.id, this.currentCellId);
	}
	
	// modifie la map cible
	public void setTargetMap(int mapId) {
		this.currentMapsPath = PathsCache.toMap(mapId, this.mapNode.map.id, this.currentCellId);
	}
	
	// retourne une chemin de cellules vers une cellule cible
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
	
	// retourne la durée en millisecondes d'un chemin de celulles
	public int getCellsPathDuration() {
		return this.currentCellsPath.getCrossingDuration();
	}
	
	// retourne une direction vers la map cible
	public Direction nextDirectionForReachTarget() {
		if(this.currentMapsPath == null)
			return null;
		return this.currentMapsPath.nextDirection();
	}
	
	// retourne une direction aléatoire pour le parcours des aires
	public Direction nextDirectionInArea() {
		if(this.areaId == -1)
			throw new FatalError("Area not initialized.");
		if(mapNode.map.subareaId != this.areaId)
			throw new FatalError("Bad current area.");
		
		Random randomGen = new Random();
		if(this.lastDirection == -1) // pas encore initialisé
			this.lastDirection = randomGen.nextInt(4) * 2; // première direction tirée au hasard
		
		Hashtable<Integer, Integer> neighbours  = new Hashtable<Integer, Integer>();
		for(int direction = 0; direction < 8; direction += 2)
			neighbours.put(direction, mapNode.map.getNeighbourMapFromDirection(direction));
		
		// priorité à la direction opposée
		Map map = MapsCache.loadMap(neighbours.get(this.lastDirection));
		if(map != null && map.mapType == 0 && map.subareaId == mapNode.map.subareaId) {
			int mapChangementCell = mapNode.getOutgoingCellId(this.lastDirection);
			if(mapChangementCell != -1)
				return new Direction(this.lastDirection, mapChangementCell);
		}
		
		int incomingDirection = getOppositeDirection(this.lastDirection);
		neighbours.remove(this.lastDirection); // on ne peut pas aller à la direction opposée
		neighbours.remove(incomingDirection); // pour éviter les retours en arrière
		
		int randomDirection;
		while(neighbours.size() > 0) {
			randomDirection = Collections.list(neighbours.keys()).get(randomGen.nextInt(neighbours.size())); // on prend une direction au hasard
			map = MapsCache.loadMap(neighbours.get(randomDirection));
			if(map != null && map.mapType == 0 && map.subareaId == mapNode.map.subareaId) {
				this.lastDirection = randomDirection;
				int mapChangementCell = mapNode.getOutgoingCellId(this.lastDirection);
				if(mapChangementCell != -1)
					return new Direction(this.lastDirection, mapChangementCell);
					
			}
			neighbours.remove(randomDirection);
		}
		this.lastDirection = incomingDirection;
		return new Direction(this.lastDirection, mapNode.getOutgoingCellId(this.lastDirection));
	}
	
	private int getOppositeDirection(int direction) {
		if(direction >= 4)
			return direction - 4;
		else
			return direction + 4;
	}
	
	public static class Direction {
		public int direction;
		public int outgoingCellId;
		
		public Direction(int direction, int outgoingCellId) {
			this.direction = direction;
			this.outgoingCellId = outgoingCellId;
		}
	}
}