package roleplay.movement.pathfinding;

import java.util.Vector;

import roleplay.movement.Cell;

public abstract class Pathfinder {
	public static final int RIGHT = 0;
	public static final int DOWN_RIGHT = 1;
	public static final int DOWN = 2;
	public static final int DOWN_LEFT = 3;
	public static final int LEFT = 4;
	public static final int UP_LEFT = 5;
	public static final int UP = 6;
	public static final int UP_RIGHT = 7;
	protected Cell[] cells;
	protected PathNode currentNode;
	protected PathNode destNode;
	protected Vector<PathNode> path;
	protected Vector<PathNode> openedList;
	protected Vector<PathNode> closedList;
	
	public Pathfinder() {}
	
	public abstract Vector<PathNode> compute(int srcId, int destId);
	protected abstract PathNode nodeIsInList(PathNode node, Vector<PathNode> list);
	protected abstract Vector<PathNode> getNeighbourNodes(int id);
	protected abstract PathNode getNeighbourNodeFromDirection(int srcId, int direction);
	
	protected static PathNode getBestNodeOfList(Vector<PathNode> list) {
		if(list.size() == 0)
			return null;
		PathNode currentNode = list.firstElement();
		for(PathNode listNode : list)
			if(listNode.cost < currentNode.cost)
				currentNode = listNode;
		return currentNode;
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
	
    public abstract class PathNode {
    	protected PathNode parent;
    	protected double cost;
    	protected int direction;
    	
    	protected PathNode(int direction, PathNode parent) {
    		this.parent = parent;
    		this.direction = direction;
    	}
    	
    	protected abstract boolean equals(PathNode node);
    	protected abstract double distanceTo(PathNode node);
    }
}