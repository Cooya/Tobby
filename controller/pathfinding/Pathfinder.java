package controller.pathfinding;

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
    	System.out.println(currentNode);
    	if(currentNode == null)
    		throw new Error("Invalid current node id.");
    	destNode = getNodeFromId(destId);
    	System.out.println(destNode);
    	if(destNode == null)
    		throw new Error("Invalid destination node id.");
    	openedList = new Vector<PathNode>();
    	closedList = new Vector<PathNode>();
		Vector<PathNode> neighbours;
		PathNode inListNode;
		
		while(!currentNode.equals(destNode)) {
			neighbours = getNeighbourNodes(currentNode);
			for(PathNode neighbourNode : neighbours) {
				System.out.println(neighbourNode + " " + neighbourNode.isAccessible);
				if(!neighbourNode.isAccessible) // obstacle
					continue;
				if(nodeIsInList(neighbourNode, closedList) != null) // déjà traitée
					continue;				
				if((inListNode = nodeIsInList(neighbourNode, openedList)) != null) { // déjà une possibilité
					if(neighbourNode.g < inListNode.g)
						inListNode = neighbourNode; // modification de la référence dans la liste
				}
				else
					openedList.add(neighbourNode);	
			}
			closedList.add(currentNode);
			currentNode = popBestNodeOfList(openedList);
			if(currentNode == null)
				throw new Error("None possible path found.");
		}
		
		//for(PathNode node : closedList)
			//System.out.println(node.id);
		//System.out.println();
		
		path = new Path();
		int direction = -2;
		while(currentNode != null) {
			if(direction != -2) // le noeud d'arrivée n'a pas de direction
				currentNode.direction = direction;
			direction = currentNode.lastDirection;
			path.addNode(currentNode);
			currentNode = currentNode.parent;
		}
		path.reverse();
		return path;
	}
	
	private static PathNode popBestNodeOfList(Vector<PathNode> list) {
		if(list.size() == 0)
			return null;
		PathNode currentNode = list.firstElement();
		for(PathNode listNode : list) {
			System.out.println(listNode + " " + listNode.g + " " + listNode.f);
			if(listNode.f < currentNode.f)
				currentNode = listNode;
		}
		System.out.println("best node : " + currentNode);
		list.remove(currentNode);
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
	
	protected static abstract class PathNode {
		protected int id;
		protected double x;
		protected double y;
		protected PathNode parent;
		protected double g; // distance [noeud courant / voisin]
		protected double f; // distance [noeud courant / voisin] + [voisin / noeud cible]
		protected int cost; // nombre de noeuds traversés
		protected boolean isAccessible;
		protected int lastDirection;
		protected int direction;
		
		protected PathNode(int id, int lastDirection, PathNode parent) {
			this.id = id;
			this.parent = parent;
			this.lastDirection = lastDirection;
			this.direction = -1;
			if(parent != null) {
				this.g = distanceTo(parent);	
				this.cost = this.parent.cost + 1;
			}
    		else { // noeud initial et noeud final
    			this.g = 0;
    			this.cost = 0;
    		}
		}
		
		protected void setHeuristic(PathNode destNode) {
			if(destNode != null)
				this.f = this.g + distanceTo(destNode);
		}
		
    	protected double distanceTo(PathNode node) {
    		return Math.sqrt(Math.pow(this.x - node.x, 2) + Math.pow(this.y - node.y, 2));
    	}
    	
    	protected boolean equals(PathNode node) {
    		return this.id == node.id;
    	}
    	
		protected abstract int getCrossingDuration(boolean mode);
		public abstract String toString();
	}
}