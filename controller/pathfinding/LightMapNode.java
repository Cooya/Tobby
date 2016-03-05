package controller.pathfinding;

import gamedata.d2p.Cell;
import gamedata.d2p.ankama.Map;

import java.util.Vector;

import main.FatalError;
import controller.pathfinding.Pathfinder.PathNode;

class LightMapNode extends PathNode {
	protected static final int MIDDLE_RIGHT_CELL = 279; // (Map.CELLS_COUNT - 1) / 2
	protected static final int MIDDLE_DOWN_CELL = 552; // (Map.CELLS_COUNT - 1 - Map.WIDTH) + (Map.WIDTH / 2)
	protected static final int MIDDLE_LEFT_CELL = 280; // Map.CELLS_COUNT * (Map.HEIGHT / 2)
	protected static final int MIDDLE_UP_CELL = 7; // Map.WIDTH / 2
	protected Map map;
	
	protected LightMapNode(Map map) {
		super(map.id, -1, null);
		this.map = map;
	}
	
	public LightMapNode(int id, int lastDirection, PathNode parent) { // juste pour la classe fille MapNode
		super(id, lastDirection, parent);
	}

	protected Cell getNeighbourCellFromDirection(int srcId, int direction) {
		int destId;
		int offsetId;
		if((srcId / Map.WIDTH) % 2 == 0) offsetId = 0;
		else offsetId = 1;
		switch(direction) {
			case Map.RIGHT :
				destId = srcId + 1;
				if(destId % Map.WIDTH != 0)
					return this.map.cells.get(destId);
				return null;
			case Map.DOWN_RIGHT :
				destId = srcId + Map.WIDTH + offsetId;
				if(destId < Map.CELLS_COUNT && (srcId + 1) % (Map.WIDTH * 2) != 0)
					return this.map.cells.get(destId);
				return null;
			case Map.DOWN :
				destId = srcId + Map.WIDTH * 2;
				if(destId < Map.CELLS_COUNT)
					return this.map.cells.get(destId);
				return null;
			case Map.DOWN_LEFT :
				destId = srcId + Map.WIDTH - 1 + offsetId;
				if(destId < Map.CELLS_COUNT && srcId % (Map.WIDTH * 2) != 0)
					return this.map.cells.get(destId);
				return null;
			case Map.LEFT :
				destId = srcId - 1;
				if(srcId % Map.WIDTH != 0)
					return this.map.cells.get(destId);
				return null;
			case Map.UP_LEFT :
				destId = srcId - Map.WIDTH - 1 + offsetId;
				if(destId >= 0 && srcId % (Map.WIDTH * 2) != 0)
					return this.map.cells.get(destId);
				return null;
			case Map.UP :
				destId = srcId - Map.WIDTH * 2;
				if(destId >= 0)
					return this.map.cells.get(destId);
				return null;
			case Map.UP_RIGHT :
				destId = srcId - Map.WIDTH + offsetId;
				if(destId > 0 && (srcId + 1) % (Map.WIDTH * 2) != 0)
					return this.map.cells.get(destId);
				return null;
		}
		throw new FatalError("Invalid direction.");
	}
	
	protected int getMapChangementCell(int direction) {
		switch(direction) {
			case Map.RIGHT : return getNearestCellIdForMapChangement(MIDDLE_RIGHT_CELL, direction);
			case Map.DOWN : return getNearestCellIdForMapChangement(MIDDLE_DOWN_CELL, direction);
			case Map.LEFT : return getNearestCellIdForMapChangement(MIDDLE_LEFT_CELL, direction);
			case Map.UP : return getNearestCellIdForMapChangement(MIDDLE_UP_CELL, direction);
			default : throw new FatalError("Invalid direction for changing map.");
		}
	}
	
	private int getNearestCellIdForMapChangement(int cellId, int direction) {
		Cell targetCell = this.map.cells.get(cellId);
		if(targetCell.isAccessibleDuringRP() && targetCell.allowsChangementMap())
			return cellId;
		
		int currentDirection;
		if(direction == Map.LEFT || direction == Map.RIGHT)
			currentDirection = Map.UP;
		else
			currentDirection = Map.LEFT;
		
		Cell cell = getNeighbourCellFromDirection(cellId, currentDirection);
		while(cell != null) {
			if(cell.isAccessibleDuringRP() && cell.allowsChangementMap())
				return cell.id;
			cell = getNeighbourCellFromDirection(cell.id, currentDirection);
		}
		
		if(direction == Map.LEFT || direction == Map.RIGHT)
			currentDirection = Map.DOWN;
		else
			currentDirection = Map.RIGHT;
		
		cell = getNeighbourCellFromDirection(cellId, currentDirection);
		while(cell != null) {
			if(cell.isAccessibleDuringRP() && cell.allowsChangementMap())
				return cell.id;
			cell = getNeighbourCellFromDirection(cell.id, currentDirection);
		}
		return -1; // pas de cellule de changement de map disponible pour cette direction
	}
	
	protected Vector<Cell> getNeighboursCell(int cellId) {
		Vector<Cell> neighbours = new Vector<Cell>();
		Cell cell;
		for(int i = 0; i < 8; ++i) {
			cell = getNeighbourCellFromDirection(cellId, i);
			if(cell != null)
				neighbours.add(cell);
		}
		return neighbours;
	}

	@Override
	protected void setNode() {
		
	}

	@Override
	protected int getCrossingDuration(boolean mode) {
		return 0;
	}

	@Override
	public String toString() {
		return this.id + " [" + this.x + ", " + this.y + "]";
	}
}