package roleplay.movement;

import java.util.Vector;

import roleplay.movement.ankama.Map;
import roleplay.movement.ankama.MapPoint;
import roleplay.movement.ankama.MovementPath;
import roleplay.movement.ankama.PathElement;

public class Pathfinder {
	public static final boolean DEBUG = false;
	public static final int RIGHT = 0;
	public static final int DOWN_RIGHT = 1;
	public static final int DOWN = 2;
	public static final int DOWN_LEFT = 3;
	public static final int LEFT = 4;
	public static final int UP_LEFT = 5;
	public static final int UP = 6;
	public static final int UP_RIGHT = 7;
	private static Cell[] cells = new Cell[Map.CELLS_COUNT];
	private static Vector<PathNode> path;
	private static PathNode currentNode;
	private static Cell dest;
	
	public static void initMap(Map map) {
    	for(int i = 0; i < Map.CELLS_COUNT; ++i)
    		cells[i] = map.cells.get(i);
    }
	
	public static MovementPath compute(int srcId, int destId) {
    	path = new Vector<PathNode>();
    	currentNode = new PathNode(getCellFromId(srcId));
    	path.add(currentNode);
    	dest = getCellFromId(destId);
    	
    	if(DEBUG)
    		System.out.println(currentNode.cell.id);
    	
		PathNode next;
		while(!currentNode.cell.equals(dest)) {
			next = getNextCell();
			if(next != null) {
				path.add(next);
				currentNode = next;
				
				if(DEBUG)
					System.out.println(currentNode.cell.id);
			}
			else { // retour en arrière
				path.remove(path.lastElement());
				currentNode = path.lastElement();
			}
		}
		return movementPathFromArray(path);
	}
	
	public static Cell getCellFromId(int cellId) {
		if(cellId < 0 || cellId > 559)
			throw new Error("Invalid cell id");
		return cells[cellId];
	}
	
	public static Cell getCellFromCoords(double x, double y) {
		double y2 = y / Cell.HALF_HEIGHT - 1;
		double x2 = x % Cell.WIDTH != 0 ? (x - Cell.HALF_WIDTH) / Cell.WIDTH : x / Cell.WIDTH - 1;
		return getCellFromId((int) (y2 * Map.WIDTH + x2));
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
	
	public static void printPath() {
		PathNode lastNode = path.lastElement();
		for(PathNode node : path)
			if(node != lastNode)
				System.out.print(node.cell.id + "->");
		System.out.println(lastNode.cell.id + "\n");
	}
	
	public static int getChangementMapCell(int direction) {
		switch(direction) {
			case RIGHT : return getNearestCellId(279, direction); // (Map.CELLS_COUNT - 1) / 2
			case DOWN : return getNearestCellId(552, direction); // (Map.CELLS_COUNT - 1 - Map.WIDTH) + (Map.WIDTH / 2)
			case LEFT : return getNearestCellId(280, direction); // Map.CELLS_COUNT * (Map.HEIGHT / 2)
			case UP : return getNearestCellId(7, direction); // Map.WIDTH / 2
			default : throw new Error("Invalid direction for changing map.");
		}
	}
	
	private static PathNode getNextCell() {
		int direction = determineDirection(currentNode.cell, dest);
		Vector<PathNode> possibilities = getPossibilities(currentNode, direction);
		if(possibilities != null)
			return chooseNextCell(possibilities);
		possibilities = getPossibilities(currentNode, direction);
		if(possibilities != null)
			return chooseNextCell(possibilities);
		return null; // aucune possibilité, il faut revenir en arrière
	}
	
	private static PathNode chooseNextCell(Vector<PathNode> possibilities) { // choisit la cellule la plus proche de la destination
		int nbPossibilities = possibilities.size();
		double minDistance = Double.MAX_VALUE;
		PathNode nearestNode = null;
		double distance;
		PathNode node = null;
		for(int i = 0; i < nbPossibilities; ++i) {
			node = possibilities.get(i);
			distance = distanceBetween(node.cell, currentNode.cell) + distanceBetween(node.cell, dest);
			if(distance < minDistance) {
				minDistance = distance;
				nearestNode = node;
			}
		}
		return nearestNode;
	}
	
	private static Vector<PathNode> getPossibilities(PathNode currentNode, int direction) {
		Vector<PathNode> possibilities = new Vector<PathNode>();
		PathNode node;
		
		int[] directions = besideDirections(direction);
		for(int i = 0; i < 3; ++i) {
			node = new PathNode(getNeighbourCellFromDirection(directions[i]), directions[i]);
			
			if(DEBUG)
				System.out.println(directions[i] + " " + node);
			
			if(node != null && currentNode.checkCell(node.cell))
				possibilities.add(node);
		}
		if(possibilities.size() > 0)
			return possibilities;
		
		directions = otherDirections(direction);
		for(int i = 0;  i < 5; ++i) {
			node = new PathNode(getNeighbourCellFromDirection(directions[i]), directions[i]);
			
			if(DEBUG)
				System.out.println(directions[i] + " " + node);
			
			if(node != null && currentNode.checkCell(node.cell))
				possibilities.add(node);
		}
		if(possibilities.size() > 0)
			return possibilities;
		return null;
	}
	
	private static int getNearestCellId(int cellId, int direction) {
		if(getCellFromId(cellId).isAccessibleDuringRP())
			return cellId;
		
		int currentDirection;
		if(direction == LEFT || direction == RIGHT)
			currentDirection = UP;
		else
			currentDirection = LEFT;
		
		Cell cell = getNeighbourCellFromDirection(cellId, currentDirection);
		while(cell != null) {
			if(cell != null && cell.isAccessibleDuringRP())
				return cell.id;
			cell = getNeighbourCellFromDirection(cell, currentDirection);
		}
		
		if(direction == LEFT || direction == RIGHT)
			currentDirection = DOWN;
		else
			currentDirection = RIGHT;
		
		cell = getNeighbourCellFromDirection(cellId, currentDirection);
		while(cell != null) {
			if(cell != null && cell.isAccessibleDuringRP())
				return cell.id;
			cell = getNeighbourCellFromDirection(cell, currentDirection);
		}
		return -1;
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
	
	private static Cell getNeighbourCellFromDirection(int direction) {
		return getNeighbourCellFromDirection(currentNode.cell.id, direction);
	}
	
	private static int determineDirection(Cell src, Cell dest) {
		if(src.x == dest.x)
			if(src.y > dest.y)
				return UP;
			else
				return DOWN;
		else if(src.y == dest.y)
			if(src.x > dest.x)
				return LEFT;
			else
				return RIGHT;
		else if(src.x > dest.x)
			if(src.y > dest.y)
				return UP_LEFT;
			else
				return DOWN_LEFT;
		else
			if(src.y > dest.y)
				return UP_RIGHT;
			else
				return DOWN_RIGHT;
	}
	
	private static int[] besideDirections(int direction) {
		int[] result = new int[3];
		result[0] = direction;
		result[1] = direction - 1 >= 0 ? direction - 1 : 7;
		result[2] = direction + 1 <= 7 ? direction + 1 : 0;
		return result;
	}
	
	private static int[] otherDirections(int direction) {
		int[] result = new int[5];
		int start = direction + 1 <= 7 ? direction + 1 : 0;
		start = start + 1 <= 7 ? start + 1 : 0;  // pour ne pas la prendre en compte dans la boucle
		int end = direction - 1 >= 0 ? direction - 1 : 7;
		for(int k = 0, i = start; i != end; ++k, ++i) {
			result[k] = direction;
			if(i == 7)
				i = -1;
		}
		return result;
	}
	
	private static double distanceBetween(Cell src, Cell dest) {
		return Math.sqrt(Math.pow(dest.x - src.x, 2) + Math.pow(dest.y - src.y, 2));
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
    
    public static class PathNode {
    	protected static final int WALK_DURATION = 500;
    	protected static final int DIAGONAL_RUN_DURATION = 200;
    	protected static final int STRAIGHT_RUN_DURATION = 333;
    	protected Cell cell;
    	protected int direction;
    	protected Vector<Cell> checkedCells;
    	
    	protected PathNode(Cell cell, int direction) {
    		this.cell = cell;
    		this.direction = direction;
    		this.checkedCells = new Vector<Cell>();
    	}
    	
    	protected PathNode(Cell cell) {
    		this(cell, -1);
    	}
    	
    	protected PathNode(int cellId, int direction) {
    		this(getCellFromId(cellId), direction);
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