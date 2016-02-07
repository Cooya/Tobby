package roleplay.movement;

import java.util.Collections;
import java.util.Vector;

import roleplay.movement.ankama.Map;
import roleplay.movement.ankama.MapPoint;
import roleplay.movement.ankama.MovementPath;
import roleplay.movement.ankama.PathElement;

public class Pathfinder {
	public static final int RIGHT = 0;
	public static final int DOWN_RIGHT = 1;
	public static final int DOWN = 2;
	public static final int DOWN_LEFT = 3;
	public static final int LEFT = 4;
	public static final int UP_LEFT = 5;
	public static final int UP = 6;
	public static final int UP_RIGHT = 7;
	private static Cell[] cells = new Cell[Map.CELLS_COUNT];
	private static PathNode currentNode;
	private static Cell src;
	private static Cell dest;
	private static Vector<PathNode> path;
	private static Vector<PathNode> openedList;
	private static Vector<PathNode> closedList;
	
	public static void initMap(Map map) {
    	for(int i = 0; i < Map.CELLS_COUNT; ++i)
    		cells[i] = map.cells.get(i);
    }
	
	public static MovementPath compute(int srcId, int destId) {
		src = getCellFromId(srcId);
		dest = getCellFromId(destId);	
    	currentNode = new PathNode(src);
    	openedList = new Vector<PathNode>();
    	closedList = new Vector<PathNode>();
    	closedList.add(currentNode);
    	Cell cell;
    	PathNode neighbourNode;
		Vector<Cell> neighbours;
		
		while(!currentNode.cell.equals(dest)) {
			neighbours = getNeighboursCell(currentNode.cell.id);
			for(int i = 0; i < 8; ++i) {
				cell = neighbours.get(i);
				if(cell == null) // cellule inexistante (bords de map)
					continue;
				if(!cell.isAccessibleDuringRP()) // obstacle
					continue;
				if(cellIsInList(cell, closedList) != null) // déjà traitée
					continue;				
				
				if((neighbourNode = cellIsInList(cell, openedList)) != null) { // déjà une possibilité
					if(currentNode.cost < neighbourNode.cost)
						neighbourNode = currentNode;
				}
				else
					openedList.add(new PathNode(cell, i, currentNode));	
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
		
		return movementPathFromArray(path);
	}
	
	private static PathNode getBestNodeOfList(Vector<PathNode> list) {
		if(list.size() == 0)
			return null;
		PathNode currentNode = list.firstElement();
		for(PathNode listNode : list)
			if(listNode.cost < currentNode.cost)
				currentNode = listNode;
		return currentNode;
	}
	
	private static PathNode cellIsInList(Cell cell, Vector<PathNode> list) {
		for(PathNode node : list)
			if(node.cell.equals(cell))
				return node;
		return null;
	}
	
	private static Vector<Cell> getNeighboursCell(int cellId) {
		Vector<Cell> neighbours = new Vector<Cell>();
		for(int i = 0; i < 8; ++i)
			neighbours.add(getNeighbourCellFromDirection(cellId, i));
		return neighbours;		
	}
	
	private static Cell getNeighbourCellFromDirection(int srcId, int direction) {
		int offsetId;
		if((srcId / Map.WIDTH) % 2 == 0) offsetId = 0;
		else offsetId = 1;
		switch(direction) {
			case RIGHT :
				if(srcId + 1 < Map.CELLS_COUNT)
					return cells[srcId + 1];
				return null;
			case DOWN_RIGHT :
				if(srcId + Map.WIDTH + offsetId < Map.CELLS_COUNT)
					return cells[srcId + Map.WIDTH + offsetId];
				return null;
			case DOWN :
				if(srcId + Map.WIDTH * 2 < Map.CELLS_COUNT)
					return cells[srcId + Map.WIDTH * 2];
				return null;
			case DOWN_LEFT :
				if(srcId + Map.WIDTH - 1 + offsetId < Map.CELLS_COUNT)
					return cells[srcId + Map.WIDTH - 1 + offsetId];
				return null;
			case LEFT :
				if(srcId - 1 > 0)
					return cells[srcId - 1];
				return null;
			case UP_LEFT :
				if(srcId - Map.WIDTH + offsetId > 0)
					return cells[srcId - Map.WIDTH + offsetId];
				return null;
			case UP :
				if(srcId - Map.WIDTH * 2 > 0)
					return cells[srcId - Map.WIDTH * 2];
				return null;
			case UP_RIGHT :
				if(srcId - Map.WIDTH + offsetId > 0)
					return cells[srcId - Map.WIDTH + offsetId];
				return null;
		}
		throw new Error("Invalid direction.");
	}
	
	private static Cell getNeighbourCellFromDirection(Cell srcCell, int direction) {
		return getNeighbourCellFromDirection(srcCell.id, direction);
	}
	
	@SuppressWarnings("unused")
	private static Cell getNeighbourCellFromDirection(int direction) {
		return getNeighbourCellFromDirection(currentNode.cell.id, direction);
	}
	
	public static Cell getCellFromId(int cellId) {
		if(cellId < 0 || cellId > 559)
			throw new Error("Invalid cell id");
		return cells[cellId];
	}
	
	private static double distanceBetween(Cell src, Cell dest) {
		return Math.sqrt(Math.pow(dest.x - src.x, 2) + Math.pow(dest.y - src.y, 2));
	}
	
	private static int getNearestCellIdForMapChangement(int cellId, int direction) {
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
			if(cell != null && cell.isAccessibleDuringRP() && cell.allowsChangementMap())
				return cell.id;
			cell = getNeighbourCellFromDirection(cell, currentDirection);
		}
		
		if(direction == LEFT || direction == RIGHT)
			currentDirection = DOWN;
		else
			currentDirection = RIGHT;
		
		cell = getNeighbourCellFromDirection(cellId, currentDirection);
		while(cell != null) {
			if(cell != null && cell.isAccessibleDuringRP() && cell.allowsChangementMap())
				return cell.id;
			cell = getNeighbourCellFromDirection(cell, currentDirection);
		}
		return -1;
	}
	
	// fonction traduite mais légèrement modifiée
    private static MovementPath movementPathFromArray(Vector<PathNode> nPath) {
    	MovementPath mp = new MovementPath();
    	int cPathLen = nPath.size();
    	Vector<MapPoint> mpPath = new Vector<MapPoint>();
    	for(int i = 0; i < cPathLen; ++i)
    		mpPath.add(MapPoint.fromCellId(nPath.get(i).cell.id));
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
	
	public static int getPathTime() {
		int pathLen = path.size();
		int time = 0;
		for(int i = 1; i < pathLen; ++i) // on saute la première cellule
			if(pathLen > 3)
				time += path.get(i).getCrossingDuration(true); // run
			else
				time += path.get(i).getCrossingDuration(false); // walk
		return time;
	}
	
	public static int getChangementMapCell(int direction) {
		switch(direction) {
			case RIGHT : return getNearestCellIdForMapChangement(279, direction); // (Map.CELLS_COUNT - 1) / 2
			case DOWN : return getNearestCellIdForMapChangement(552, direction); // (Map.CELLS_COUNT - 1 - Map.WIDTH) + (Map.WIDTH / 2)
			case LEFT : return getNearestCellIdForMapChangement(280, direction); // Map.CELLS_COUNT * (Map.HEIGHT / 2)
			case UP : return getNearestCellIdForMapChangement(7, direction); // Map.WIDTH / 2
			default : throw new Error("Invalid direction for changing map.");
		}
	}
	
	public static String directionToString(int direction) {
		switch(direction) {
			case 0 : return "right";
			case 1 : return "down and right";
			case 2 : return "down";
			case 3 : return "down and left";
			case 4 : return "left";
			case 5 : return "top and left";
			case 6 : return "top";
			case 7 : return "top and right";
			default : throw new Error("Invalid direction integer.");
		}
	}
	
    public static class PathNode {
    	protected static final int WALK_DURATION = 500;
    	protected static final int DIAGONAL_RUN_DURATION = 200;
    	protected static final int STRAIGHT_RUN_DURATION = 333;
    	protected Cell cell;
    	protected PathNode parent;
    	protected double cost;
    	protected int direction;
    	protected Vector<Cell> checkedCells;
    	
    	protected PathNode(Cell cell, int direction, PathNode parent) {
    		this.cell = cell;
    		this.parent = parent;
    		this.checkedCells = new Vector<Cell>();
    		if(parent != null)
    			this.cost = distanceBetween(parent.cell, cell) + distanceBetween(dest, cell);
    		else
    			this.cost = distanceBetween(dest, cell);
    		this.direction = direction;
    	}
    	
    	protected PathNode(Cell cell) {
    		this(cell, -1, null);
    	}
    	
    	protected PathNode(int cellId, int direction, PathNode parent) {
    		this(getCellFromId(cellId), direction, parent);
    	}
    	
    	protected int getCrossingDuration(boolean mode) {
    		if(!mode) // marche
    			return WALK_DURATION;
    		if(this.direction % 2 == 0)
    			return STRAIGHT_RUN_DURATION;
    		else
    			return DIAGONAL_RUN_DURATION;
    	}
    	
    	protected boolean checkCell(Cell cell) {
    		for(Cell checkedCell : this.checkedCells)
    			if(checkedCell.equals(cell))
    				return false;
    		this.checkedCells.add(cell);
    		return cell.isAccessibleDuringRP();
    	}
    }
}
