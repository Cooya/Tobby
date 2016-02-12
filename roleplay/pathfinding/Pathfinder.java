package roleplay.pathfinding;

import java.util.Vector;

public abstract class Pathfinder {
	public static final int RIGHT = 0;
	public static final int DOWN_RIGHT = 1;
	public static final int DOWN = 2;
	public static final int DOWN_LEFT = 3;
	public static final int LEFT = 4;
	public static final int UP_LEFT = 5;
	public static final int UP = 6;
	public static final int UP_RIGHT = 7;
	protected PathNode currentNode;
	protected PathNode destNode;
	protected Path path;
	protected Vector<PathNode> openedList;
	protected Vector<PathNode> closedList;
	
	protected abstract PathNode getNodeFromId(int id);
	protected abstract PathNode nodeIsInList(PathNode node, Vector<PathNode> list);
	protected abstract Vector<PathNode> getNeighbourNodes(PathNode node);
	
	public Path compute(int srcId, int destId) {
    	currentNode = getNodeFromId(srcId);
    	destNode = getNodeFromId(destId);
    	openedList = new Vector<PathNode>();
    	closedList = new Vector<PathNode>();
    	closedList.add(currentNode);
		Vector<PathNode> neighbours;
		PathNode inListNode;
		
		while(!currentNode.equals(destNode)) {
			neighbours = getNeighbourNodes(currentNode);
			for(PathNode neighbourNode : neighbours) {
				System.out.println(neighbourNode + " " + neighbourNode.isAccessible());
				if(!neighbourNode.isAccessible()) // obstacle
					continue;
				if(nodeIsInList(neighbourNode, closedList) != null) // d�j� trait�e
					continue;				
				if((inListNode = nodeIsInList(neighbourNode, openedList)) != null) { // d�j� une possibilit�
					if(currentNode.cost < inListNode.cost)
						inListNode = currentNode; // modification de la r�f�rence dans la liste
				}
				else
					openedList.add(neighbourNode);	
			}
			System.out.println(currentNode);
			currentNode = getBestNodeOfList(openedList);
			System.out.println(currentNode);
			if(currentNode == null)
				throw new Error("None possible path found.");
			openedList.remove(currentNode);
			closedList.add(currentNode);
		}
		
		//for(PathNode node : closedList)
			//System.out.println(node.id);
		
		path = new Path();
		while(currentNode != null) {
			path.addNode(currentNode);
			currentNode = currentNode.parent;
		}
		path.reverse();
		return path;
	}
	
	protected static PathNode getBestNodeOfList(Vector<PathNode> list) {
		if(list.size() == 0)
			return null;
		PathNode currentNode = list.firstElement();
		for(PathNode listNode : list) {
			System.out.println(listNode + " " + listNode.cost);
			if(listNode.cost < currentNode.cost)
				currentNode = listNode;
		}
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
}