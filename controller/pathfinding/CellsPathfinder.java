package controller.pathfinding;

import gamedata.d2p.Cell;
import gamedata.d2p.ankama.Map;
import gamedata.d2p.ankama.MapPoint;
import gamedata.d2p.ankama.MovementPath;
import gamedata.d2p.ankama.PathElement;

import java.util.Vector;

import main.FatalError;

public class CellsPathfinder extends Pathfinder {
	private Cell[] cells;
	
	public CellsPathfinder(Map map) {
		cells = new Cell[Map.CELLS_COUNT];
		for(int i = 0; i < Map.CELLS_COUNT; ++i)
    		cells[i] = map.cells.get(i);
	}
	
	protected PathNode getNodeFromId(int cellId) {
		return new CellNode(cellId);
	}
	
	protected Vector<PathNode> getNeighbourNodes(PathNode node) {
		Vector<PathNode> neighbours = new Vector<PathNode>();
		Cell cell;
		for(int direction = 0; direction < 8; ++direction) {
			cell = getNeighbourCellFromDirection(node.id, direction);
			if(cell != null)
				neighbours.add(new CellNode(cell, direction, this.currentNode));
		}
		return neighbours;		
	}

	protected PathNode nodeIsInList(PathNode node, Vector<PathNode> list) {
		CellNode cn = (CellNode) node;
		for(PathNode pn : list)
			if(pn.id == cn.id)
				return pn;
		return null;
	}
	
	private Cell getNeighbourCellFromDirection(int srcId, int direction) {
		int destId;
		int offsetId;
		if((srcId / Map.WIDTH) % 2 == 0) offsetId = 0;
		else offsetId = 1;
		switch(direction) {
			case RIGHT :
				destId = srcId + 1;
				if(destId % Map.WIDTH != 0)
					return cells[destId];
				return null;
			case DOWN_RIGHT :
				destId = srcId + Map.WIDTH + offsetId;
				if(destId < Map.CELLS_COUNT && (srcId + 1) % (Map.WIDTH * 2) != 0)
					return cells[destId];
				return null;
			case DOWN :
				destId = srcId + Map.WIDTH * 2;
				if(destId < Map.CELLS_COUNT)
					return cells[destId];
				return null;
			case DOWN_LEFT :
				destId = srcId + Map.WIDTH - 1 + offsetId;
				if(destId < Map.CELLS_COUNT && srcId % (Map.WIDTH * 2) != 0)
					return cells[destId];
				return null;
			case LEFT :
				destId = srcId - 1;
				if(srcId % Map.WIDTH != 0)
					return cells[destId];
				return null;
			case UP_LEFT :
				destId = srcId - Map.WIDTH - 1 + offsetId;
				if(destId >= 0 && srcId % (Map.WIDTH * 2) != 0)
					return cells[destId];
				return null;
			case UP :
				destId = srcId - Map.WIDTH * 2;
				if(destId >= 0)
					return cells[destId];
				return null;
			case UP_RIGHT :
				destId = srcId - Map.WIDTH + offsetId;
				if(destId > 0 && (srcId + 1) % (Map.WIDTH * 2) != 0)
					return cells[destId];
				return null;
		}
		throw new FatalError("Invalid direction.");
	}

	private int getNearestCellIdForMapChangement(int cellId, int direction) {
		Cell targetCell = getCellFromId(cellId);
		if(targetCell.isAccessibleDuringRP() && targetCell.allowsChangementMap())
			return cellId;
		
		int currentDirection;
		if(direction == LEFT || direction == RIGHT)
			currentDirection = UP;
		else
			currentDirection = LEFT;
		
		Cell cell = getNeighbourCellFromDirection(cellId, currentDirection);
		while(cell != null) {
			if(cell.isAccessibleDuringRP() && cell.allowsChangementMap())
				return cell.id;
			cell = getNeighbourCellFromDirection(cell.id, currentDirection);
		}
		
		if(direction == LEFT || direction == RIGHT)
			currentDirection = DOWN;
		else
			currentDirection = RIGHT;
		
		cell = getNeighbourCellFromDirection(cellId, currentDirection);
		while(cell != null) {
			if(cell.isAccessibleDuringRP() && cell.allowsChangementMap())
				return cell.id;
			cell = getNeighbourCellFromDirection(cell.id, currentDirection);
		}
		return -1;
	}
	
	public Vector<Cell> getNeighboursCell(int cellId) {
		Vector<Cell> neighbours = new Vector<Cell>();
		Cell cell;
		for(int i = 0; i < 8; ++i) {
			cell = getNeighbourCellFromDirection(cellId, i);
			if(cell != null)
				neighbours.add(cell);
		}
		return neighbours;
	}
	
	public int getChangementMapCell(int direction) {
		switch(direction) {
			case RIGHT : return getNearestCellIdForMapChangement(279, direction); // (Map.CELLS_COUNT - 1) / 2
			case DOWN : return getNearestCellIdForMapChangement(552, direction); // (Map.CELLS_COUNT - 1 - Map.WIDTH) + (Map.WIDTH / 2)
			case LEFT : return getNearestCellIdForMapChangement(280, direction); // Map.CELLS_COUNT * (Map.HEIGHT / 2)
			case UP : return getNearestCellIdForMapChangement(7, direction); // Map.WIDTH / 2
			default : throw new FatalError("Invalid direction for changing map.");
		}
	}
	
	// fonction traduite mais légèrement modifiée
    public static MovementPath movementPathFromArray(Vector<Integer> iPath) {
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
    
	private Cell getCellFromId(int cellId) {
		if(cellId < 0 || cellId > 559)
			throw new FatalError("Invalid cell id");
		return cells[cellId];
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
		
    	private CellNode(Cell cell) {
    		this(cell, -1, null);
    	}
    	
    	private CellNode(int cellId) {
    		this(getCellFromId(cellId));
    	}
    	
		private CellNode(int cellId, int lastDirection, PathNode parent) {
    		this(getCellFromId(cellId), lastDirection, parent);
    	}
    	
		@SuppressWarnings("unused")
		private boolean checkCell(Cell cell) {
    		for(Cell checkedCell : this.checkedCells)
    			if(checkedCell.equals(cell))
    				return false;
    		this.checkedCells.add(cell);
    		return cell.isAccessibleDuringRP();
    	}
    	
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
    	
    	public String toString() {
    		if(this.direction != -1)
    			return String.valueOf(this.id) + " [" + this.x + ", " + this.y + "] " + Pathfinder.directionToString(this.direction);
    		return String.valueOf(this.id) + " [" + this.x + ", " + this.y + "]";
    	}
	}
}