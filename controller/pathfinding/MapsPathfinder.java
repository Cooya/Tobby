package controller.pathfinding;

import gamedata.d2o.modules.MapPosition;
import gamedata.d2p.Cell;
import gamedata.d2p.MapsCache;
import gamedata.d2p.ankama.Map;

import java.util.Vector;

public class MapsPathfinder extends Pathfinder {
	private static int[] LEFT_CELL_IDS = new int[40];
	private static int[] RIGHT_CELL_IDS = new int[40];
	private static int[] UP_CELL_IDS = new int[28];
	private static int[] DOWN_CELL_IDS = new int[28];
	private int startCellId;
	
	static {
		for(int i = 0, j = 0; i < 560; i += 14, j++)
			LEFT_CELL_IDS[j] = i;
		for(int i = 13, j = 0; i < 560; i += 14, j++)
			RIGHT_CELL_IDS[j] = i;
		for(int i = 0, j = 0; i < 28; i++, j++)
			UP_CELL_IDS[j] = i;
		for(int i = 532, j = 0; i < 560; i++, j++)
			DOWN_CELL_IDS[j] = i;
	}
	
	protected MapsPathfinder(int startCellId) {
		this.startCellId = startCellId;
	}
	
	protected MapsPathfinder() {
		this.startCellId = -1;
	}
	
	protected PathNode getNodeFromId(int mapId) {
		return new MapNode(mapId);
	}

	protected PathNode nodeIsInList(PathNode node, Vector<PathNode> list) {
		MapNode mn = (MapNode) node;
		for(PathNode pn : list)
			if(((MapNode) pn).map == mn.map) // on peut utiliser la référence ici
				return pn;
		return null;
	}

	protected Vector<PathNode> getNeighbourNodes(PathNode node) {
		Vector<PathNode> neighbours = new Vector<PathNode>();
		Map map;
		for(int direction = 0; direction < 8; direction += 2) {
			map = getMapFromId(((MapNode) node).map.getNeighbourMapFromDirection(direction));
			if(map != null)
				neighbours.add(new MapNode(map, direction, node));
		}
		return neighbours;	
	}
	
	private Map getMapFromId(int mapId) {
		MapPosition mp = MapPosition.getMapPositionById(mapId);
		if(mp == null)
			return null;
		return MapsCache.loadMap(mapId);
	}
	
	protected boolean inSameZone(int mapId, int cellId1, int cellId2) {
		MapNode node = new MapNode(mapId, -1, null);
		return node.getCurrentZone(cellId1) == node.getCurrentZone(cellId2);		
	}
	
	private class MapNode extends PathNode {
		private Map map;
		@SuppressWarnings("unused")
		private int worldId;
		private Cell cell;
		private Vector<Vector<Cell>> zones;
		
		private MapNode(Map map, int lastDirection, PathNode parent) {
			super(map.id, lastDirection, parent);
			this.map = map;
			this.zones = MapsAnalyser.getZones(map);
			MapPosition mp = MapPosition.getMapPositionById(map.id);
			this.worldId = mp.worldMap; // est-ce la même chose ?!
			this.x = mp.posX;
			this.y = mp.posY;
			if(lastDirection != -1)
				setCurrentCell();
			else { // cellule de départ et d'arrivée
				if(startCellId == -1) // création de path à distance
					this.cell = map.cells.get(getFirstAccessibleCellId());
				else
					this.cell = map.cells.get(startCellId); // s'applique aussi au noeud de destination, mais cela ne change rien
			}
			this.isAccessible = this.cell != null;
			setHeuristic(destNode);
		}
		
		private MapNode(int mapId, int lastDirection, PathNode parent) {
    		this(getMapFromId(mapId), lastDirection, parent);
    	}
    	
    	private MapNode(int mapId) {
    		this(getMapFromId(mapId), -1, null);
    	}
		
		private void setCurrentCell() {
			int[] directionCellIds;
			switch(this.lastDirection) {
				case LEFT : directionCellIds = LEFT_CELL_IDS; break;
				case RIGHT : directionCellIds = RIGHT_CELL_IDS; break; 
				case UP : directionCellIds = UP_CELL_IDS; break;
				case DOWN : directionCellIds = DOWN_CELL_IDS; break;
				default : throw new Error("Invalid direction for changing map.");
			}
			
			Cell cell;
			Vector<Cell> parentCurrentZone = ((MapNode) this.parent).getCurrentZone();
			if(parentCurrentZone == null)
				throw new Error("Invalid parent current cell.");
			for(int directionCellId : directionCellIds)
				for(Cell parentCell : parentCurrentZone)
					if(parentCell.allowsChangementMap() && directionCellId == parentCell.id) {
						cell = getNewCellAfterMapChangement(directionCellId, this.lastDirection);
						if(cell.isAccessibleDuringRP()) {
							this.cell = cell;
							return;
						}
					}
			// map inaccessible donc this.cell vaut null
		}
		
		private int getFirstAccessibleCellId() {
			for(Cell cell : this.map.cells)
				if(cell.isAccessibleDuringRP())
					return cell.id;
			throw new Error("Map without available cell ! Impossible !");
		}
		
		private Vector<Cell> getCurrentZone(int cellId) {
			//System.out.println("currentMap : " + MapPosition.getMapPositionById(currentNode.id));
			//System.out.println("currrentCell : " + ((MapNode) currentNode).cell.id);
			Vector<Cell> currentZone = null;
			boolean found = false;
			for(Vector<Cell> zone : ((MapNode) currentNode).zones)
				if(!found)
					for(Cell cell : zone)
						if(cell.id == cellId) {
							currentZone = zone;
							found = true;
							break;
						}
			return currentZone;
		}
		
		private Vector<Cell> getCurrentZone() {
			return getCurrentZone(((MapNode) currentNode).cell.id);
		}
		
		private Cell getNewCellAfterMapChangement(int srcId, int direction) {
			switch(direction) {
				case RIGHT : return this.map.cells.get(srcId + 1 - (Map.WIDTH - 1));
				case LEFT : return this.map.cells.get(srcId - 1 + (Map.WIDTH - 1));
				case UP : return this.map.cells.get((srcId - Map.WIDTH * 2) + 560);
				case DOWN : return this.map.cells.get((srcId + Map.WIDTH * 2) - 560);
			}
			throw new Error("Invalid direction for changing map.");
		}
		
    	protected int getCrossingDuration(boolean mode) {
    		return 1;
    	}
    	
    	public String toString() {
    		if(this.direction != -1)
    			return this.id + " [" + this.x + ", " + this.y + "] " + Pathfinder.directionToString(this.direction);
    		return this.id + " [" + this.x + ", " + this.y + "]";
    	}
	}
}