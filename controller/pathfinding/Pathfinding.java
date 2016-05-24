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
	private Hashtable<Integer, Integer> neighbourMaps;
	
	public Pathfinding() {
		this.currentCellId = -1;
		this.lastDirection = -1;
		this.areaId = -1;
		this.neighbourMaps = new Hashtable<Integer, Integer>(4);
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
		if(mapNode.map.subareaId != this.areaId)
			throw new FatalError("Bad current area.");
		
		Random randomGen = new Random();
		int neighboursNumber;
		Map map;
		int randomDirection;
		int mapChangementCell;
		
		// on tire une direction au hasard s'il n'y a pas de dernière direction
		if(this.lastDirection == -1)
			this.lastDirection = randomGen.nextInt(4) * 2;
		
		// on récupère chaque map voisine
		for(int direction = 0; direction < 8; direction += 2)
			this.neighbourMaps.put(direction, mapNode.map.getNeighbourMapFromDirection(direction));
		
		// on retire la map voisine correspondant à la map précédente pour éviter les retours en arrière
		int incomingDirection = getOppositeDirection(this.lastDirection);
		this.neighbourMaps.remove(incomingDirection);
		
		// il reste donc 3 directions possibles
		while((neighboursNumber = this.neighbourMaps.size()) > 0) {
			// on récupère la map voisine correspondant à une direction au hasard
			randomDirection = Collections.list(this.neighbourMaps.keys()).get(randomGen.nextInt(neighboursNumber));
			map = MapsCache.loadMap(this.neighbourMaps.get(randomDirection));
			
			// si la map existe et qu'elle est dans la même aire
			if(map != null && map.mapType == 0 && map.subareaId == mapNode.map.subareaId) {
				// on tente de déterminer la cellule de changement de map
				mapChangementCell = mapNode.getOutgoingCellId(randomDirection);
				if(mapChangementCell != -1) {
					this.lastDirection = randomDirection;
					return new Direction(this.lastDirection, mapChangementCell);
				}
					
			}
			this.neighbourMaps.remove(randomDirection);
		}
		
		// si aucune de ces 3 directions n'est atteignable, on fait marche arrière
		this.lastDirection = incomingDirection;
		return new Direction(this.lastDirection, mapNode.getOutgoingCellId(this.lastDirection));
	}
	
	// retourne la direction opposée
	private int getOppositeDirection(int direction) {
		if(direction >= 4)
			return direction - 4;
		else
			return direction + 4;
	}
	
	// représente une direction composée du sens et de la cellule sortante de la map
	public static class Direction {
		public int direction;
		public int outgoingCellId;
		
		public Direction(int direction, int outgoingCellId) {
			this.direction = direction;
			this.outgoingCellId = outgoingCellId;
		}
	}
}