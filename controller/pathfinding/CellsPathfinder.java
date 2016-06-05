package controller.pathfinding;

import gamedata.d2p.Cell;
import gamedata.d2p.ankama.Map;
import gamedata.d2p.ankama.MapPoint;
import gamedata.d2p.ankama.MovementPath;
import gamedata.d2p.ankama.PathElement;

import java.util.Vector;

class CellsPathfinder extends Pathfinder {
	private LightMapNode mapNode;
	
	protected CellsPathfinder(Map map) {
		this.mapNode = new LightMapNode(map);
	}
	
	@Override
	protected PathNode getNodeFromId(int cellId) {
		return new CellNode(cellId);
	}
	
	@Override
	protected PathNode nodeIsInList(PathNode node, Vector<PathNode> list) {
		CellNode cn = (CellNode) node;
		for(PathNode pn : list)
			if(pn.id == cn.id)
				return pn;
		return null;
	}
	
	@Override
	protected Vector<PathNode> getNeighbourNodes(PathNode node) {
		Vector<PathNode> neighbours = new Vector<PathNode>();
		Cell cell;
		for(int direction = 0; direction < 8; ++direction) {
			cell = this.mapNode.getNeighbourCellFromDirection(node.id, direction);
			if(cell != null)
				neighbours.add(new CellNode(cell, direction, this.currentNode));
		}
		return neighbours;		
	}
	
	// fonction traduite mais légèrement modifiée
    protected static MovementPath movementPathFromArray(Vector<Integer> iPath) {
    	MovementPath mp = new MovementPath();
    	Vector<MapPoint> mpPath = new Vector<MapPoint>();
    	for(Integer cellId : iPath)
    		mpPath.add(MapPoint.fromCellId(cellId));
    	int vectorSize = mpPath.size();
    	PathElement pe;
    	for(int i = 0; i < vectorSize - 1; ++i) {
    		pe = new PathElement(null, 0);
    		pe.getStep().setX(mpPath.get(i).getX());
    		pe.getStep().setY(mpPath.get(i).getY());
    		pe.setOrientation(mpPath.get(i).orientationTo(mpPath.get(i + 1)));
    		mp.addPoint(pe);
    	}
    	mp.compress();
    	mp.fill();
    	return mp;
    }
    
	private class CellNode extends PathNode {
    	private static final int HORIZONTAL_WALK_DURATION = 510;
    	private static final int VERTICAL_WALK_DURATION = 425;
    	private static final int DIAGONAL_WALK_DURATION = 480;
    	private static final int HORIZONTAL_RUN_DURATION = 255;
    	private static final int VERTICAL_RUN_DURATION = 150;
    	private static final int DIAGONAL_RUN_DURATION = 170;
		private Vector<Cell> checkedCells;
		
		private CellNode(Cell cell, int lastDirection, PathNode parent) {
			super(cell.id, lastDirection, parent);
			this.x = cell.x;
			this.y = cell.y;
    		this.isAccessible = cell.isAccessibleDuringRP();
			setHeuristic(destNode);
			this.checkedCells = new Vector<Cell>();
		}
    	
    	private CellNode(int cellId) {
    		this(mapNode.map.cells[cellId], -1, null);
    	}
    	
		@SuppressWarnings("unused")
		private boolean checkCell(Cell cell) {
    		for(Cell checkedCell : this.checkedCells)
    			if(checkedCell.equals(cell))
    				return false;
    		this.checkedCells.add(cell);
    		return cell.isAccessibleDuringRP();
    	}

		@Override
		protected void setNode() {
			
		}
    	
		@Override
    	protected int getCrossingDuration(boolean mode) {
    		if(!mode) { // walk
    			if(this.lastDirection % 2 == 0) {
    				if(this.lastDirection % 4 == 0) // left or right
    					return HORIZONTAL_WALK_DURATION;
    				else // top or down
    					return VERTICAL_WALK_DURATION;
    			}
    			else // other directions
    				return DIAGONAL_WALK_DURATION;
    		}
    		else { // run
    			if(this.lastDirection % 2 == 0) {
    				if(this.lastDirection % 4 == 0) // left or right
    					return HORIZONTAL_RUN_DURATION;
    				else // top or down
    					return VERTICAL_RUN_DURATION;
    			}
    			else // other directions
    				return DIAGONAL_RUN_DURATION;
    		}
    	}
    	
		@Override
    	public String toString() {
    		if(this.direction != -1)
    			return String.valueOf(this.id) + " [" + this.x + ", " + this.y + "] " + Map.directionToString(this.direction);
    		return String.valueOf(this.id) + " [" + this.x + ", " + this.y + "]";
    	}
	}
}