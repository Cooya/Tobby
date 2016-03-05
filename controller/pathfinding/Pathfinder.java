package controller.pathfinding;

import java.util.Vector;

import main.FatalError;

abstract class Pathfinder {
	protected PathNode currentNode;
	protected PathNode destNode;
	protected Path path;
	protected Vector<PathNode> openedList;
	protected Vector<PathNode> closedList;
	
	protected abstract PathNode getNodeFromId(int id);
	protected abstract PathNode nodeIsInList(PathNode node, Vector<PathNode> list);
	protected abstract Vector<PathNode> getNeighbourNodes(PathNode node);
	
	protected Path compute(int srcId, int destId) {
    	currentNode = getNodeFromId(srcId);
    	//System.out.println(currentNode);
    	if(currentNode == null)
    		throw new FatalError("Invalid current node id.");
    	destNode = getNodeFromId(destId);
    	//System.out.println(destNode);
    	if(destNode == null)
    		throw new FatalError("Invalid destination node id.");
    	openedList = new Vector<PathNode>();
    	closedList = new Vector<PathNode>();
		Vector<PathNode> neighbours;
		PathNode inListNode;
		
		while(!currentNode.equals(destNode)) {
			neighbours = getNeighbourNodes(currentNode);
			for(PathNode neighbourNode : neighbours) {
				//System.out.println(neighbourNode + " " + neighbourNode.g + " " + Pathfinder.directionToString(neighbourNode.lastDirection) + " " + neighbourNode.isAccessible);
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
			//System.out.println();
			closedList.add(currentNode);
			currentNode = popBestNodeOfList(openedList);
			if(currentNode == null) // pas de chemin possible
				return null;
		}
		
		//for(PathNode node : closedList)
			//System.out.println(node.id);
		//System.out.println();
		
		path = new Path();
		int direction = -2;
		while(currentNode != null) {
			if(direction != -2) // le noeud d'arrivée n'a pas de direction
				currentNode.direction = direction;
			currentNode.setNode();
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
			//System.out.println(listNode + " " + listNode.g + " " + listNode.f);
			if(listNode.f < currentNode.f)
				currentNode = listNode;
		}
		//System.out.println("best node : " + currentNode + " " + currentNode.h);
		list.remove(currentNode);
		return currentNode;
	}
	
	protected static abstract class PathNode {
		protected int id;
		protected double x;
		protected double y;
		protected PathNode parent;
		protected double g; // distance [noeud courant / parent]
		protected double f; // distance [noeud courant / parent] + [noeud courant / noeud cible]
		protected double h; // distance [noeud courant / cible]
		protected int cost; // nombre de noeuds traversés
		protected boolean isAccessible;
		protected int lastDirection;
		protected int direction;
		protected int outgoingCellId; // uniquement pour les MapNodes
		
		protected PathNode(int id, int lastDirection, PathNode parent) {
			this.id = id;
			this.parent = parent;
			this.lastDirection = lastDirection;
			this.direction = -1;
		}
		
		protected void setHeuristic(PathNode destNode) {
			if(parent != null) {
				this.g = parent.g + distanceTo(parent);
				this.cost = this.parent.cost + 1;
			}
			else { // noeud initial et noeud final
				this.g = 0;
    			this.cost = 0;
			}
			if(destNode != null) {
				this.h = distanceTo(destNode);
				this.f = this.g + this.h;
			}
		}
		
    	protected double distanceTo(PathNode node) {
    		return Math.sqrt(Math.pow(node.x - this.x, 2) + Math.pow(node.y - this.y, 2));
    	}
    	
    	protected boolean equals(PathNode node) {
    		return this.id == node.id;
    	}
    	
    	protected abstract void setNode();
		protected abstract int getCrossingDuration(boolean mode);
		public abstract String toString();
	}
}