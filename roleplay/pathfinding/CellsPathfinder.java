package roleplay.pathfinding;

import java.util.Vector;

import roleplay.d2p.Cell;
import roleplay.d2p.ankama.Map;
import roleplay.d2p.ankama.MapPoint;
import roleplay.d2p.ankama.MovementPath;
import roleplay.d2p.ankama.PathElement;

public class CellsPathfinder extends Pathfinder {
	protected Cell[] cells;
	
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
		for(int i = 0; i < 8; ++i) {
			cell = getNeighbourCellFromDirection(node.id, i);
			if(cell != null)
				neighbours.add(new CellNode(cell, i, currentNode));
		}
		return neighbours;		
	}

	protected PathNode nodeIsInList(PathNode node, Vector<PathNode> list) {
		CellNode cn = (CellNode) node;
		for(PathNode pn : list)
			if(((CellNode) pn).cell == cn.cell) // on peut utiliser la référence ici
				return pn;
		return null;
	}
	
	private Cell getNeighbourCellFromDirection(int srcId, int direction) {
		int offsetId;
		if((srcId / Map.WIDTH) % 2 == 0) offsetId = 0;
		else offsetId = 1;
		switch(direction) {
			case RIGHT :
				if((srcId + 1) % Map.WIDTH != 0)
					return cells[srcId + 1];
				return null;
			case DOWN_RIGHT :
				if(srcId + Map.WIDTH + offsetId < Map.CELLS_COUNT && (srcId + 1) % Map.WIDTH != 0)
					return cells[srcId + Map.WIDTH + offsetId];
				return null;
			case DOWN :
				if(srcId + Map.WIDTH * 2 < Map.CELLS_COUNT)
					return cells[srcId + Map.WIDTH * 2];
				return null;
			case DOWN_LEFT :
				if(srcId + Map.WIDTH - 1 + offsetId < Map.CELLS_COUNT && srcId % Map.WIDTH != 0)
					return cells[srcId + Map.WIDTH - 1 + offsetId];
				return null;
			case LEFT :
				if(srcId % Map.WIDTH != 0)
					return cells[srcId - 1];
				return null;
			case UP_LEFT :
				if(srcId - Map.WIDTH - 1 + offsetId > 0 && srcId % Map.WIDTH != 0)
					return cells[srcId - Map.WIDTH - 1 + offsetId];
				return null;
			case UP :
				if(srcId - Map.WIDTH * 2 > 0)
					return cells[srcId - Map.WIDTH * 2];
				return null;
			case UP_RIGHT :
				if(srcId - Map.WIDTH + offsetId > 0 && (srcId + 1) % Map.WIDTH != 0)
					return cells[srcId - Map.WIDTH + offsetId];
				return null;
		}
		throw new Error("Invalid direction.");
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
			default : throw new Error("Invalid direction for changing map.");
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
			throw new Error("Invalid cell id");
		return cells[cellId];
	}
	
	private class CellNode extends PathNode {
    	private static final int WALK_DURATION = 500;
    	private static final int DIAGONAL_RUN_DURATION = 200;
    	private static final int STRAIGHT_RUN_DURATION = 333;
    	protected Cell cell;
		private Vector<Cell> checkedCells;
		
		private CellNode(Cell cell, int direction, PathNode parent) {
			super(cell.id, direction, parent);
			this.cell = cell;
			this.checkedCells = new Vector<Cell>();
    		if(destNode == null) // si on est en train de définir destNode lui-même
    			return;
    		if(parent != null)
    			this.cost = distanceTo(parent) + distanceTo(destNode);
    		else
    			this.cost = distanceTo(destNode);
		}
		
    	private CellNode(Cell cell) {
    		this(cell, -1, null);
    	}
    	
    	private CellNode(int cellId) {
    		this(getCellFromId(cellId));
    	}
    	
		private CellNode(int cellId, int direction, PathNode parent) {
    		this(getCellFromId(cellId), direction, parent);
    	}
    	
		@SuppressWarnings("unused")
		private boolean checkCell(Cell cell) {
    		for(Cell checkedCell : this.checkedCells)
    			if(checkedCell.equals(cell))
    				return false;
    		this.checkedCells.add(cell);
    		return cell.isAccessibleDuringRP();
    	}
    	
    	protected boolean equals(PathNode node) {
    		if(!(node instanceof CellNode))
    			throw new Error("Invalid type.");
    		CellNode cn = (CellNode) node;
    		return this.cell.equals(cn.cell);
    	}
    	
    	protected double distanceTo(PathNode node) {
    		if(!(node instanceof CellNode))
    			throw new Error("Invalid type.");
    		CellNode cn = (CellNode) node;
    		return Math.sqrt(Math.pow(this.cell.x - cn.cell.x, 2) + Math.pow(this.cell.y - cn.cell.y, 2));
    	}
    	
    	protected boolean isAccessible() {
    		return this.cell.isAccessibleDuringRP();
    	}
    	
    	protected int getCrossingDuration(boolean mode) {
    		if(!mode) // marche
    			return WALK_DURATION;
    		if(this.direction % 2 == 0)
    			return STRAIGHT_RUN_DURATION;
    		else
    			return DIAGONAL_RUN_DURATION;
    	}
    	
    	public String toString() {
    		return String.valueOf(this.id);
    	}
	}
}