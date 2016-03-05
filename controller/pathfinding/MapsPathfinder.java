package controller.pathfinding;

import gamedata.d2o.modules.MapPosition;
import gamedata.d2p.Cell;
import gamedata.d2p.MapsCache;
import gamedata.d2p.ankama.Map;

import java.util.Vector;

import main.FatalError;

class MapsPathfinder extends Pathfinder {
	private int startCellId;
	
	protected MapsPathfinder(int startCellId) {
		this.startCellId = startCellId;
	}
	
	@Override
	protected PathNode getNodeFromId(int mapId) {
		return new MapNode(mapId, -1, null, this.startCellId);
	}
	
	@Override
	protected PathNode nodeIsInList(PathNode node, Vector<PathNode> list) {
		MapNode mn = (MapNode) node;
		for(PathNode pn : list)
			if(((MapNode) pn).map == mn.map) // on peut utiliser la référence ici
				return pn;
		return null;
	}

	@Override
	protected Vector<PathNode> getNeighbourNodes(PathNode node) {
		Vector<PathNode> neighbours = new Vector<PathNode>();
		Map map;
		for(int direction = 0; direction < 8; direction += 2) {
			map = getMapFromId(((MapNode) node).map.getNeighbourMapFromDirection(direction));
			if(map != null)
				neighbours.add(new MapNode(map, direction, node, this.startCellId));
		}
		return neighbours;	
	}
	
	private Map getMapFromId(int mapId) {
		MapPosition mp = MapPosition.getMapPositionById(mapId);
		if(mp == null)
			return null;
		return MapsCache.loadMap(mapId);
	}
	
	private class MapNode extends LightMapNode {
		private MapZones zones;
		private Vector<Cell> currentZone;
		private Vector<Cell> outgoingPossibilities;
		
		protected MapNode(Map map, int lastDirection, PathNode parent, int incomingCellId) {
			super(map.id, lastDirection, parent);
			this.map = map;
			this.zones = new MapZones(map);
			this.outgoingPossibilities = new Vector<Cell>();
			MapPosition mp = MapPosition.getMapPositionById(map.id);
			this.x = mp.posX;
			this.y = mp.posY;
			this.outgoingCellId = -1;
			if(lastDirection != -1)
				setCurrentCell();
			else { // cellule de départ et d'arrivée
				if(incomingCellId == -1) // création de path à distance
					this.currentZone = this.zones.getZone(getFirstAccessibleCellId()); // pas très fiable
				else
					this.currentZone = this.zones.getZone(incomingCellId); // s'applique aussi au noeud de destination, mais cela ne change rien
			}
			this.isAccessible = this.currentZone != null;
			setHeuristic(destNode);
		}
		
		protected MapNode(int mapId, int lastDirection, PathNode parent, int incomingCellId) {
			this(MapsCache.loadMap(mapId), lastDirection, parent, incomingCellId);
		}
		
		private void setCurrentCell() {
			Cell cell;
			Vector<Cell> parentCurrentZone = ((MapNode) this.parent).currentZone;
			if(parentCurrentZone == null)
				throw new FatalError("Invalid parent current cell.");
			for(Cell parentCell : parentCurrentZone)
				if(parentCell.allowsChangementMap() && isOutgoingPossibility(parentCell.id, this.lastDirection)) {
					cell = getNewCellAfterMapChangement(parentCell.id, this.lastDirection);
					if(cell.isAccessibleDuringRP()) {
						//System.out.println(toString() + " " + Pathfinder.directionToString(this.lastDirection) + " " + directionCellId + " " + cell);
						this.currentZone = this.zones.getZone(cell.id);
						((MapNode) this.parent).outgoingPossibilities.add(parentCell);
						return;
					}
				}
		}
		
		private int getFirstAccessibleCellId() {
			for(Cell cell : this.map.cells)
				if(cell.isAccessibleDuringRP())
					return cell.id;
			throw new FatalError("Map without available cell ! Impossible !");
		}
		
		private Cell getNewCellAfterMapChangement(int srcId, int direction) {
			switch(direction) {
				case Map.RIGHT : return this.map.cells.get(srcId + 1 - (Map.WIDTH - 1));
				case Map.LEFT : return this.map.cells.get(srcId - 1 + (Map.WIDTH - 1));
				case Map.UP : return this.map.cells.get((srcId - Map.WIDTH * 2) + 560);
				case Map.DOWN : return this.map.cells.get((srcId + Map.WIDTH * 2) - 560);
			}
			throw new FatalError("Invalid direction for changing map.");
		}
		
		private boolean isOutgoingPossibility(int cellId, int direction) {
			switch(direction) {
				case Map.LEFT : return cellId % Map.WIDTH == 0;
				case Map.RIGHT : return (cellId + 1) % Map.WIDTH == 0;
				case Map.UP : return cellId < 28;
				case Map.DOWN : return cellId > 532;
				default : throw new FatalError("Invalid direction for changing map.");
			}
		}
		
		private int getOutgoingCellId() {
			Cell middleCell;
			switch(this.direction) {
				case Map.RIGHT : middleCell = this.map.cells.get(MIDDLE_RIGHT_CELL); break;
				case Map.DOWN : middleCell = this.map.cells.get(MIDDLE_DOWN_CELL); break;
				case Map.LEFT : middleCell = this.map.cells.get(MIDDLE_LEFT_CELL); break;
				case Map.UP : middleCell = this.map.cells.get(MIDDLE_UP_CELL); break;
				default : throw new FatalError("Invalid direction for changing map.");
			}	
			
			Cell nearestCell = this.currentZone.firstElement();
			double shortestDistance = Cell.distanceBetween(nearestCell, middleCell);
			double currentDistance;
			for(Cell cell : this.currentZone) {
				if(cell.allowsChangementMap()) {
					currentDistance = Cell.distanceBetween(cell, middleCell);
					if(currentDistance == 0)
						return cell.id;
					if(currentDistance < shortestDistance) {
						shortestDistance = currentDistance;
						nearestCell = cell;
					}
				}
			}
			return nearestCell.id;
		}
		
		@Override
		protected void setNode() {
			for(Cell cell : this.outgoingPossibilities)
				if(isOutgoingPossibility(cell.id, this.direction)) {
					this.outgoingCellId = getOutgoingCellId();
					break;
				}
		}
		
		@Override
		protected int getCrossingDuration(boolean mode) {
			return 1;
		}
		
		@Override
		public String toString() {
			if(this.direction != -1)
				return this.id + " [" + this.x + ", " + this.y + "] " + Map.directionToString(this.direction) + " " + this.outgoingCellId;
			return this.id + " [" + this.x + ", " + this.y + "]";
		}
	}
}