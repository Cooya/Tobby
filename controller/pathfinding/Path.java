package controller.pathfinding;

import gamedata.d2p.ankama.Map;

import java.util.Collections;
import java.util.Vector;

import controller.pathfinding.Pathfinder.PathNode;
import controller.pathfinding.Pathfinding.Direction;
import main.FatalError;

class Path {
	private String name;
	private Vector<PathNode> nodes;
	private int currentPos;
	private boolean isLoop;
	protected int startCellId = -1; // uniquement pour les paths de maps
	
	protected Path(Vector<PathNode> nodes) {
		this.name = "anonymous";
		this.nodes = nodes;
		this.currentPos = 0;
		this.isLoop = false;
	}
	
	protected Path(String name, boolean isLoop) {
		this.name = name;
		this.nodes = new Vector<PathNode>();
		this.currentPos = 0;
		this.isLoop = isLoop;
	}
	
	protected Path() {
		this("anonymous", false);
	}
	
	public Direction nextDirection() {
		if(this.isLoop && this.currentPos == this.nodes.size()) // absolument inutile car il ne rend pas la main
			this.currentPos = 0;
		else if(this.currentPos == this.nodes.size() - 1) // on ne s'intéresse pas au noeud d'arrivée
			return null;
		PathNode currentNode = this.nodes.get(this.currentPos++);
		return new Direction(currentNode.direction, currentNode.outgoingCellId);
	}
	
	public String getName() {
		return this.name;
	}
	
	protected PathNode getFirstNode() {
		return this.nodes.firstElement();
	}
	
	protected PathNode getLastNode() {
		return this.nodes.lastElement();
	}
	
	protected void resetPosition() {
		this.currentPos = 0;
	}
	
	protected int getCrossingDuration() {
		int pathLen = nodes.size();
		int time = 0;
		for(int i = 1; i < pathLen; ++i) // on saute la première cellule
			if(pathLen > 3)
				time += nodes.get(i).getCrossingDuration(true); // courir
			else
				time += nodes.get(i).getCrossingDuration(false); // marcher
		return time;
	}
	
	protected Vector<Integer> toVector() {
		Vector<Integer> vector = new Vector<Integer>();
		for(PathNode node : nodes)
			vector.add(node.id);
		return vector;
	}
	
	public String toString() {
		String str = "Path \"" + this.name + "\" : \n";
		for(PathNode node : nodes)
			str += node + "\n";
		return str;
	}
	
	protected void addNode(int id, int direction) {
		if(direction != Map.LEFT && direction != Map.RIGHT && direction != Map.UP && direction != Map.DOWN)
			throw new FatalError("Invalid direction for create a node path.");
		this.nodes.add(new SimplePathNode(id, direction));
	}
	
	protected void addNode(PathNode node) {
		this.nodes.add(node);
	}
	
	protected void reverse() {
		Collections.reverse(nodes);
	}
	
	@SuppressWarnings("unused")
	private boolean onPath(int currentMapId) {
		int vectorSize = nodes.size();
		for(int i = 0; i < vectorSize; ++i)
			if(nodes.get(i).id == currentMapId) {
				this.currentPos = i;
				return true;
			}
		return false;
	}
	
	private class SimplePathNode extends PathNode { // pour les paths enregistrés dans le fichier "paths.txt"

		private SimplePathNode(int id, int direction) {
			super(id, -1, null);
			this.direction = direction;
		}

		protected int getCrossingDuration(boolean mode) {
			throw new FatalError("Phony method !");
		}
		
		protected void setNode() {
			
		}
		
		public String toString() {
			return String.valueOf(this.id);
		}
	}
}