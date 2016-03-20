package controller.pathfinding;

import gamedata.d2p.Cell;
import gamedata.d2p.ankama.Map;

import java.util.Vector;

import main.FatalError;
import controller.pathfinding.Pathfinder.PathNode;

// noeud utilisé pour les parcours de zones (pas besoin de création de chemin)
class LightMapNode extends PathNode {
	private static final int MIDDLE_RIGHT_CELL_ID = 279; // (Map.CELLS_COUNT - 1) / 2
	private static final int MIDDLE_DOWN_CELL_ID = 552; // (Map.CELLS_COUNT - 1 - Map.WIDTH) + (Map.WIDTH / 2)
	private static final int MIDDLE_LEFT_CELL_ID = 280; // Map.CELLS_COUNT * (Map.HEIGHT / 2)
	private static final int MIDDLE_UP_CELL_ID = 7; // Map.WIDTH / 2
	private static final int[] FORBIDDEN_CELL_IDS = {0, 14, 27, 532, 545, 559};
	protected Map map;
	protected MapZones zones;
	protected Vector<Cell> currentZone;
	
	protected LightMapNode(Map map, int currentCellId) { // version plus complexe
		super(map.id, -1, null);
		this.map = map;
		this.zones = new MapZones(map);
		this.currentZone = this.zones.getZone(currentCellId);
	}
	
	protected LightMapNode(Map map) { // version simple
		super(map.id, -1, null);
		this.map = map;
	}
	
	public LightMapNode(int id, int lastDirection, PathNode parent) { // juste pour la classe fille MapNode
		super(id, lastDirection, parent);
	}

	// retourne la cellule voisine selon une certaine direction
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
	
	
	// indique si une cellule permet le changement de map selon une certaine direction
	protected static boolean isOutgoingPossibility(int cellId, int direction) {
		switch(direction) {
			case Map.LEFT : return cellId % Map.WIDTH == 0;
			case Map.RIGHT : return (cellId + 1) % Map.WIDTH == 0;
			case Map.UP : return cellId < 28;
			case Map.DOWN : return cellId > 532;
			default : throw new FatalError("Invalid direction for changing map.");
		}
	}
	
	// indique si une cellule est dans un coin de la map ou pas (pour éviter le piège des doubles directions)
	protected static boolean isForbiddenPossibility(int cellId) {
		for(int i : FORBIDDEN_CELL_IDS)
			if(i == cellId)
				return true;
		return false;
	}
	
	// retourne, si elle existe, l'id de la cellule de changement de map la plus proche du milieu pour une certaine direction
	protected int getOutgoingCellId(int direction) {
		Cell middleCell;
		switch(direction) {
			case Map.RIGHT : middleCell = this.map.cells.get(MIDDLE_RIGHT_CELL_ID); break;
			case Map.DOWN : middleCell = this.map.cells.get(MIDDLE_DOWN_CELL_ID); break;
			case Map.LEFT : middleCell = this.map.cells.get(MIDDLE_LEFT_CELL_ID); break;
			case Map.UP : middleCell = this.map.cells.get(MIDDLE_UP_CELL_ID); break;
			default : throw new FatalError("Invalid direction for changing map.");
		}	
		
		int nearestCellId = -1;
		double shortestDistance = Double.MAX_VALUE;
		double currentDistance;
		for(Cell cell : this.currentZone) {
			if(cell.allowsChangementMap() && isOutgoingPossibility(cell.id, direction) && !isForbiddenPossibility(cell.id)) {
				currentDistance = Cell.distanceBetween(cell, middleCell);
				if(currentDistance == 0) // petit raccourci
					return cell.id;
				if(currentDistance < shortestDistance) {
					shortestDistance = currentDistance;
					nearestCellId = cell.id;
				}
			}
		}
		return nearestCellId;
	}
	
	// retourne un vecteur contenant les cellules voisines de la cellule donnée
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
	
	// ancienne version
	
	/*
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
	*/
}