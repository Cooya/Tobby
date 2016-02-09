package roleplay.movement.pathfinding;

import java.util.Collections;
import java.util.Vector;

import roleplay.movement.Cell;
import roleplay.movement.ankama.Map;
import roleplay.movement.ankama.MapPoint;
import roleplay.movement.ankama.MovementPath;
import roleplay.movement.ankama.PathElement;

public class CellsPathfinder extends Pathfinder {
	
	public CellsPathfinder(Map map) {
		super();
		cells = new Cell[Map.CELLS_COUNT];
		for(int i = 0; i < Map.CELLS_COUNT; ++i)
    		cells[i] = map.cells.get(i);
	}

	public Vector<PathNode> compute(int srcId, int destId) {
    	currentNode = new CellNode(srcId);
    	destNode = new CellNode(destId);
    	openedList = new Vector<PathNode>();
    	closedList = new Vector<PathNode>();
    	closedList.add(currentNode);
		Vector<PathNode> neighbours;
		PathNode inListNode;
		
		while(!currentNode.equals(destNode)) {
			neighbours = getNeighbourNodes(((CellNode) currentNode).cell.id);
			for(PathNode neighbourNode : neighbours) {
				if(!((CellNode) neighbourNode).cell.isAccessibleDuringRP()) // obstacle
					continue;
				if(nodeIsInList(neighbourNode, closedList) != null) // déjà traitée
					continue;				
				if((inListNode = nodeIsInList(neighbourNode, openedList)) != null) { // déjà une possibilité
					if(currentNode.cost < inListNode.cost)
						inListNode = (CellNode) currentNode; // modification de la référence dans la liste
				}
				else
					openedList.add(neighbourNode);	
			}
			
			currentNode = getBestNodeOfList(openedList);
			if(currentNode == null)
				throw new Error("None possible path found.");
			openedList.remove(currentNode);
			closedList.add(currentNode);
		}
		
		//for(PathNode node : closedList)
			//System.out.println(node.cell.id);
		
		path = new Vector<Pathfinder.PathNode>();
		while(currentNode != null) {
			path.add(currentNode);
			currentNode = currentNode.parent;
		}
		
		Collections.reverse(path);
		
		return path;
	}
	
	protected Vector<PathNode> getNeighbourNodes(int cellId) {
		Vector<PathNode> neighbours = new Vector<PathNode>();
		Cell cell;
		for(int i = 0; i < 8; ++i) {
			cell = getNeighbourCellFromDirection(cellId, i);
			if(cell != null)
				neighbours.add(new CellNode(cell, i, currentNode));
		}
		return neighbours;		
	}
	
	protected PathNode getNeighbourNodeFromDirection(int srcId, int direction) {
		Cell cell = getNeighbourCellFromDirection(srcId, direction);
		if(cell != null)
			return new CellNode(cell, direction, currentNode);
		return null;
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
	
	public Cell getCellFromId(int cellId) {
		if(cellId < 0 || cellId > 559)
			throw new Error("Invalid cell id");
		return cells[cellId];
	}
	
	public int getPathTime() {
		int pathLen = path.size();
		int time = 0;
		for(int i = 1; i < pathLen; ++i) // on saute la première cellule
			if(pathLen > 3)
				time += ((CellNode) path.get(i)).getCrossingDuration(true); // run
			else
				time += ((CellNode) path.get(i)).getCrossingDuration(false); // walk
		return time;
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
    public static MovementPath movementPathFromArray(Vector<PathNode> nPath) {
    	MovementPath mp = new MovementPath();
    	Vector<MapPoint> mpPath = new Vector<MapPoint>();
    	for(PathNode node : nPath)
    		mpPath.add(MapPoint.fromCellId(((CellNode) node).cell.id));
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
	
	public class CellNode extends PathNode {
    	private static final int WALK_DURATION = 500;
    	private static final int DIAGONAL_RUN_DURATION = 200;
    	private static final int STRAIGHT_RUN_DURATION = 333;
		public Cell cell;
		private Vector<Cell> checkedCells;
		
		protected CellNode(Cell cell, int direction, PathNode parent) {
			super(direction, parent);
			this.cell = cell;
			this.checkedCells = new Vector<Cell>();
    		if(destNode == null) // si on est en train de définir destNode lui-même
    			return;
    		if(parent != null)
    			this.cost = distanceTo(parent) + distanceTo(destNode);
    		else
    			this.cost = distanceTo(destNode);
		}
		
    	protected CellNode(Cell cell) {
    		this(cell, -1, null);
    	}
    	
    	protected CellNode(int cellId) {
    		this(getCellFromId(cellId));
    	}
    	
    	protected CellNode(int cellId, int direction, PathNode parent) {
    		this(getCellFromId(cellId), direction, parent);
    	}
    	
    	protected boolean checkCell(Cell cell) {
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
    	
    	public double distanceTo(PathNode node) {
    		if(!(node instanceof CellNode))
    			throw new Error("Invalid type.");
    		CellNode cn = (CellNode) node;
    		return Math.sqrt(Math.pow(this.cell.x - cn.cell.x, 2) + Math.pow(this.cell.y - cn.cell.y, 2));
    	}
    	
    	protected int getCrossingDuration(boolean mode) {
    		if(!mode) // marche
    			return WALK_DURATION;
    		if(this.direction % 2 == 0)
    			return STRAIGHT_RUN_DURATION;
    		else
    			return DIAGONAL_RUN_DURATION;
    	}
	}
}
