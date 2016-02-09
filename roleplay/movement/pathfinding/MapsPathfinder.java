package roleplay.movement.pathfinding;

import java.util.Vector;

import roleplay.movement.Cell;
import roleplay.movement.D2pReader;
import roleplay.movement.MapsAnalyser;
import roleplay.movement.ankama.Map;

public class MapsPathfinder extends Pathfinder {
	private static int[] LEFT_CELL_IDS;
	private static int[] RIGHT_CELL_IDS;
	private static int[] UP_CELL_IDS;
	private static int[] DOWN_CELL_IDS;
	private int currentCellId;
	
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
	
	public MapsPathfinder(int currentCellId) {
		this.currentCellId = currentCellId;
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
	
	@SuppressWarnings("unused")
	private Cell getNewCellAfterMapChangement(int srcId, int direction) {
		switch(direction) {
			case RIGHT : return cells[srcId + 1 - (Map.WIDTH - 1)];
			case LEFT : return cells[srcId - 1 + (Map.WIDTH - 1)];
			case UP : return cells[(srcId - Map.WIDTH * 2) + 560];
			case DOWN : return cells[(srcId + Map.WIDTH * 2) - 560];
		}
		throw new Error("Invalid direction for changing map.");
	}
	
	private Map getMapFromId(int mapId) {
		return new Map(D2pReader.getBinaryMap(mapId));
	}
	
	private class MapNode extends PathNode {
		private Map map;
		@SuppressWarnings("unused")
		private int worldId;
		private int x;
		private int y;
		private Vector<Vector<Cell>> zones;
		
		private MapNode(Map map, int direction, PathNode parent) {
			super(direction, parent);
			this.map = map;
			this.zones = MapsAnalyser.getZones(map);
			setCoordsFromId(map.id);
		}
		
    	private MapNode(Map map) {
    		this(map, -1, null);
    	}
    	
    	private MapNode(int mapId) {
    		this(getMapFromId(mapId));
    	}
    	
		private MapNode(int mapId, int direction, PathNode parent) {
    		this(getMapFromId(mapId), direction, parent);
    	}
		
		private void setCoordsFromId(int mapId) {
			this.worldId = (mapId & 0x3FFC0000) >> 18;
			this.x = (mapId >> 9) & 511;
			this.y = mapId & 511;
			if((this.x & 0x0100) == 0x0100)
				this.x = -(this.x & 0xFF);
			if((this.y & 0x0100) == 0x0100)
				this.y = -(this.x & 0xFF);
		}
		
		protected int getId() {
			return this.map.id;
		}

		protected boolean equals(PathNode node) {
			if(!(node instanceof MapNode))
    			throw new Error("Invalid type.");
    		MapNode mn = (MapNode) node;
    		return this.map.id == mn.map.id;
		}

		protected double distanceTo(PathNode node) {
    		if(!(node instanceof MapNode))
    			throw new Error("Invalid type.");
    		MapNode mn = (MapNode) node;
    		return Math.sqrt(Math.pow(this.x - mn.x, 2) + Math.pow(this.y - mn.y, 2));
		}
		
		protected boolean isAccessible() {
			int[] directionCellIds;
			switch(direction) {
				case LEFT : directionCellIds = LEFT_CELL_IDS; break;
				case RIGHT : directionCellIds = RIGHT_CELL_IDS; break; 
				case UP : directionCellIds = UP_CELL_IDS; break;
				case DOWN : directionCellIds = DOWN_CELL_IDS; break;
				default : throw new Error("Invalid direction for changing map.");
			}
			Vector<Cell> currentZone = null;
			boolean found = false;
			for(Vector<Cell> zone : ((MapNode) currentNode).zones)
				if(!found)
					for(Cell cell : zone)
						if(cell.id == currentCellId && cell.allowsChangementMap()) {
							currentZone = zone;
							found = true;
							break;
						}
			for(Cell cell : currentZone)
				for(int i = 0; i < directionCellIds.length; ++i)
					if(directionCellIds[i] == cell.id)
						return true;
			return false;
		}
	}
}