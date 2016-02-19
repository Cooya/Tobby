package game.pathfinding;

import game.pathfinding.Pathfinder.PathNode;

import java.util.Collections;
import java.util.Vector;

import main.CharacterController;
import utilities.Log;

public class Path {
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
	
	public void run(CharacterController CC) {
		Log.p("Running path named \"" + name + "\".");

		CC.currentPathName = this.name;
		int nextMapId;
		while((nextMapId = nextMap()) != -1)
			CC.changeMap(nextMapId);
	}
	
	public String getName() {
		return this.name;
	}
	
	public PathNode getFirstNode() {
		return this.nodes.firstElement();
	}
	
	public PathNode getLastNode() {
		return this.nodes.lastElement();
	}
	
	public void resetPosition() {
		this.currentPos = 0;
	}
	
	public int getCrossingDuration() { // uniquement pour les paths de cellules
		int pathLen = nodes.size();
		int time = 0;
		for(int i = 1; i < pathLen; ++i) // on saute la première cellule
			if(pathLen > 3)
				time += nodes.get(i).getCrossingDuration(true); // courir
			else
				time += nodes.get(i).getCrossingDuration(false); // marcher
		return time;
	}
	
	public Vector<Integer> toVector() {
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
		if(direction != Pathfinder.LEFT && direction != Pathfinder.RIGHT && direction != Pathfinder.UP && direction != Pathfinder.DOWN)
			throw new Error("Invalid direction for create a node path.");
		this.nodes.add(new SimplePathNode(id, direction));
	}
	
	protected void addNode(PathNode node) {
		this.nodes.add(node);
	}
	
	protected int nextMap() {
		if(this.currentPos == this.nodes.size())
			if(this.isLoop)
				this.currentPos = 0;
			else
				return -1;
		return this.nodes.get(this.currentPos++).direction;
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
	
	private class SimplePathNode extends PathNode {

		private SimplePathNode(int id, int direction) {
			super(id, -1, null);
			this.direction = direction;
		}

		protected boolean equals(PathNode node) {
			throw new Error("Phony method !");
		}

		protected double distanceTo(PathNode node) {
			throw new Error("Phony method !");
		}

		protected boolean isAccessible() {
			throw new Error("Phony method !");
		}

		protected int getCrossingDuration(boolean mode) {
			throw new Error("Phony method !");
		}
		
		public String toString() {
			return String.valueOf(this.id);
		}
	}
}