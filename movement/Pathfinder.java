package movement;

import java.util.Vector;

import movement.ankama.CellData;
import movement.ankama.Map;

public class Pathfinder {
	public static final int MAP_WIDTH = 14;
	public static final int MAP_HEIGHT = 40; // normalement 20
	public static final int RIGHT = 0;
	public static final int DOWN_RIGHT = 1;
	public static final int DOWN = 2;
	public static final int DOWN_LEFT = 3;
	public static final int LEFT = 4;
	public static final int UP_LEFT = 5;
	public static final int UP = 6;
	public static final int UP_RIGHT = 7;
	private static Cell[][] cells = new Cell[MAP_WIDTH][MAP_HEIGHT];
	private static Vector<Cell> path;
	private static Cell currentCell;
	private static Cell dest;
	
	public static void initMap(Map map) {
		CellData cd;
		int id = 0;
    	for(int i = 0; i < MAP_WIDTH; ++i) {
    		cells[i] = new Cell[MAP_HEIGHT];
    		for(int j = 0; j < MAP_HEIGHT; ++j) {
    			cd = map.cells.get(id++);
    			cells[i][j] = new Cell(i ,j, cd.speed, cd.getNonWalkableDuringFight(), cd.getNonWalkableDuringRP());
    		}
    	}
	}
	
	public static Vector<Cell> compute(int srcId, int destId) {
    	path = new Vector<Cell>();
    	currentCell = getCellFromId(srcId);
    	path.add(currentCell);
    	dest = getCellFromId(destId);
    	
		Cell next;
		while(!currentCell.equals(dest)) {
			next = getNextCell();
			if(next != null) {
				path.add(next);
				currentCell = next;
			}
			else { // retour en arrière
				path.remove(path.lastElement());
				currentCell = path.lastElement();
			}
		}
		path.add(dest);
		return path;
	}
	
	private static Cell getNextCell() {
		int direction = determineDirection(currentCell, dest);
		Vector<Cell> possibilities = getPossibilities(direction);
		if(possibilities != null)
			return chooseNextCell(possibilities);
		possibilities = getPossibilities(direction);
		if(possibilities != null)
			return chooseNextCell(possibilities);
		return null; // aucune possibilité, il faut revenir en arrière
	}
	
	private static Cell chooseNextCell(Vector<Cell> possibilities) { // choisit la cellule la plus proche de la destination
		int nbPossibilities = possibilities.size();
		double minDistance = Double.MAX_VALUE;
		Cell nearestCell = null;
		double distance;
		Cell cell = null;
		for(int i = 0; i < nbPossibilities; ++i) {
			cell = possibilities.get(i);
			distance = distanceBetween(cell, dest);
			if(distance < minDistance) {
				minDistance = distance;
				nearestCell = cell;
			}
		}
		return nearestCell;
	}
	
	private static Vector<Cell> getPossibilities(int direction) {
		Vector<Cell> possibilities = new Vector<Cell>();
		Cell cell;
		
		int[] directions = besideDirections(direction);
		for(int i = 0; i < 3; ++i) {
			cell = getCellFromDirection(directions[i]);
			if(cell != null && cell.check())
				possibilities.add(cell);
		}
		if(possibilities.size() > 0)
			return possibilities;
		
		directions = otherDirections(direction);
		for(int i = 0;  i < 5; ++i) {
			cell = getCellFromDirection(directions[i]);
			if(cell != null && cell.check())
				possibilities.add(cell);
		}
		if(possibilities.size() > 0)
			return possibilities;
		return null;
	}
	
	private static Cell getCellFromDirection(int direction) {
		switch(direction) {
			case RIGHT :
				if(currentCell.x + 1 < MAP_WIDTH)
					return cells[currentCell.x + 1][currentCell.y];
				return null;
			case DOWN_RIGHT :
				if(currentCell.x + 1 < MAP_WIDTH && currentCell.y + 1 < MAP_HEIGHT)
					return cells[currentCell.x + 1][currentCell.y + 1];
				return null;
			case DOWN :
				if(currentCell.y + 1 < MAP_HEIGHT)
					return cells[currentCell.x][currentCell.y + 1];
				return null;
			case DOWN_LEFT :
				if(currentCell.x - 1 > 0 && currentCell.y + 1 < MAP_HEIGHT)
					return cells[currentCell.x - 1][currentCell.y + 1];
				return null;
			case LEFT :
				if(currentCell.x - 1 > 0)
					return cells[currentCell.x - 1][currentCell.y];
				return null;
			case UP_LEFT :
				if(currentCell.x - 1 > 0 && currentCell.y - 1 > 0)
					return cells[currentCell.x - 1][currentCell.y - 1];
				return null;
			case UP :
				if(currentCell.y - 1 > 0)
					return cells[currentCell.x][currentCell.y - 1];
				return null;
			case UP_RIGHT :
				if(currentCell.x + 1 < MAP_WIDTH && currentCell.y - 1 > 0)
					return cells[currentCell.x + 1][currentCell.y - 1];
				return null;
		}
		throw new Error("Invalid direction.");
	}
	
	private static Cell getCellFromId(int cellId) {
		if(cellId < 0 || cellId > 559)
			throw new Error("Invalid cell id");
		return cells[cellId % MAP_WIDTH][cellId / MAP_WIDTH];
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
}
