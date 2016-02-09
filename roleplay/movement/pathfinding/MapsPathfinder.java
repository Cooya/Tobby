package roleplay.movement.pathfinding;

import java.util.Vector;

import roleplay.movement.Cell;
import roleplay.movement.ankama.Map;

public class MapsPathfinder extends Pathfinder {

	@Override
	public Vector<PathNode> compute(int srcId, int destId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PathNode nodeIsInList(PathNode node, Vector<PathNode> list) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector<PathNode> getNeighbourNodes(int id) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected PathNode getNeighbourNodeFromDirection(int srcId, int direction) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@SuppressWarnings("unused")
	private Cell getNewCellAfterMapChangement(int srcId, int direction) {
		switch(direction) {
			case RIGHT : return cells[srcId + 1 - (Map.WIDTH - 1)];
			case LEFT : return cells[srcId - 1 + (Map.WIDTH - 1)];
			case UP : return cells[(srcId - Map.WIDTH * 2) + 560];
			case DOWN : return cells[(srcId + Map.WIDTH * 2) - 560];
		}
		throw new Error("Invalid direction for changing map.");
	}
}