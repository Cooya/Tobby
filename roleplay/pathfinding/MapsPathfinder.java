package roleplay.pathfinding;

import java.util.Vector;

import roleplay.d2o.modules.MapPosition;
import roleplay.d2p.Cell;
import roleplay.d2p.MapsCache;
import roleplay.d2p.ankama.Map;

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
	
	public MapsPathfinder(int startCellId) {
		this.startCellId = startCellId;
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
		for(int i = 0; i < 8; i += 2)
			neighbours.add(new MapNode(((MapNode) node).map.getNeighbourMapFromDirection(i), i, node));
		return neighbours;	
	}
	
	private Map getMapFromId(int mapId) {
		return MapsCache.loadMap(mapId);
	}
	
	private class MapNode extends PathNode {
		private Map map;
		@SuppressWarnings("unused")
		private int worldId;
		private int x;
		private int y;
		private Cell cell;
		private Vector<Vector<Cell>> zones;
		
		private MapNode(Map map, int direction, PathNode parent) {
			super(map.id, direction, parent);
			this.map = map;
			this.zones = MapsAnalyser.getZones(map);
			setCoordsFromId(map.id);
			if(direction != -1)
				setCurrentCell(direction);
			else
				this.cell = map.cells.get(startCellId); // s'applique aussi au noeud de destination, mais cela ne change rien
    		if(destNode == null) // si on est en train de définir destNode lui-même
    			return;
			if(parent != null)
    			this.cost = distanceTo(parent) + distanceTo(destNode);
    		else
    			this.cost = distanceTo(destNode);
		}
		
		private MapNode(int mapId, int direction, PathNode parent) {
    		this(getMapFromId(mapId), direction, parent);
    	}
    	
    	private MapNode(int mapId) {
    		this(getMapFromId(mapId), -1, null);
    	}
		
		private void setCoordsFromId(int mapId) {
			MapPosition mp = MapPosition.getMapPositionById(mapId);
			this.worldId = mp.worldMap; // est-ce la même chose ?!
			this.x = mp.posX;
			this.y = mp.posY;
		}
		
		private void setCurrentCell(int direction) {
			int[] directionCellIds;
			switch(this.direction) {
				case LEFT : directionCellIds = LEFT_CELL_IDS; break;
				case RIGHT : directionCellIds = RIGHT_CELL_IDS; break; 
				case UP : directionCellIds = UP_CELL_IDS; break;
				case DOWN : directionCellIds = DOWN_CELL_IDS; break;
				default : throw new Error("Invalid direction for changing map.");
			}
			Cell cell;
			Vector<Cell> currentZone = getCurrentZone();
			if(currentZone == null)
				throw new Error("Invalid current zone.");
			for(int directionCellId : directionCellIds)
				for(Cell zoneCell : currentZone)
					if(zoneCell.allowsChangementMap() && directionCellId == zoneCell.id) {
						cell = getNewCellAfterMapChangement(directionCellId, direction);
						if(cell.isAccessibleDuringRP()) {
							this.cell = cell;
							return;
						}
					}
			// map inaccessible donc this.cell vaut null
		}
		
		protected boolean isAccessible() {
			return this.cell != null;
		}
		
		private Vector<Cell> getCurrentZone() {
			Vector<Cell> currentZone = null;
			boolean found = false;
			for(Vector<Cell> zone : ((MapNode) currentNode).zones)
				if(!found)
					for(Cell cell : zone)
						if(cell.id == ((MapNode) currentNode).cell.id) {
							currentZone = zone;
							found = true;
							break;
						}
			return currentZone;
		}
		
		private Cell getNewCellAfterMapChangement(int srcId, int direction) {
			switch(direction) {
				case RIGHT : return MapsCache.loadMap(this.map.rightNeighbourId).cells.get(srcId + 1 - (Map.WIDTH - 1));
				case LEFT : return MapsCache.loadMap(this.map.leftNeighbourId).cells.get(srcId - 1 + (Map.WIDTH - 1));
				case UP : return MapsCache.loadMap(this.map.topNeighbourId).cells.get((srcId - Map.WIDTH * 2) + 560);
				case DOWN : return MapsCache.loadMap(this.map.bottomNeighbourId).cells.get((srcId + Map.WIDTH * 2) - 560);
			}
			throw new Error("Invalid direction for changing map.");
		}

		protected boolean equals(PathNode node) {
			if(!(node instanceof MapNode))
    			throw new Error("Invalid type.");
    		MapNode mn = (MapNode) node;
    		return this.map.id == mn.map.id;
		}
		
		protected double distanceTo(PathNode node) {
    		if(node == null || !(node instanceof MapNode))
    			throw new Error("Null node or invalid type.");
    		MapNode mn = (MapNode) node;
    		return Math.sqrt(Math.pow(this.x - mn.x, 2) + Math.pow(this.y - mn.y, 2));
		}
		
    	protected int getCrossingDuration(boolean mode) { // fonction bidon
    		return 0;
    	}
    	
    	public String toString() {
    		return this.id + " [" + this.x + ", " + this.y + "]";
    	}
	}
}