package roleplay.pathfinding;

import java.util.Collections;
import java.util.Vector;

import main.CharacterController;
import utilities.Log;

public class Path {
	private String name;
	private Vector<PathNode> nodes;
	private int currentPos;
	private boolean isLoop;
	
	public Path(Vector<PathNode> nodes) {
		this.name = "anonymous";
		this.nodes = nodes;
		this.currentPos = 0;
		this.isLoop = false;
	}
	
	public Path(String name, boolean isLoop) {
		this.name = name;
		this.nodes = new Vector<PathNode>();
		this.currentPos = 0;
		this.isLoop = isLoop;
	}
	
	public Path() {
		this("anonymous", false);
	}
	
	public void addNode(int id, int direction) {
		if(direction != Pathfinder.LEFT && direction != Pathfinder.RIGHT && direction != Pathfinder.UP && direction != Pathfinder.DOWN)
			throw new Error("Invalid direction for create a node path.");
		this.nodes.add(new SimplePathNode(id, direction));
	}
	
	public void addNode(PathNode node) {
		this.nodes.add(node);
	}
	
	public int nextMap() {
		if(this.currentPos == this.nodes.size())
			if(this.isLoop)
				this.currentPos = 0;
			else
				return -1;
		
		return this.nodes.get(this.currentPos++).direction;
	}
	
	public void resetPositionPath() {
		this.currentPos = 0;
	}
	
	public void checkCurrentPos(int currentMapId) {
		int vectorSize = nodes.size();
		for(int i = 0; i < vectorSize; ++i)
			if(nodes.get(i).id == currentMapId) {
				this.currentPos = i;
				return;
			}
		throw new Error("Impossible to run this path, invalid character position.");
	}
	
	public void run(CharacterController CC) {
		
		Log.p("Running path named \"" + name + "\".");
		
		checkCurrentPos(CC.getCurrentMapId()); // vérifie si le perso est bien sur le trajet
		CC.setCurrentPathName(this.name);
		int nextMapId;
		while((nextMapId = nextMap()) != -1)
			CC.changeMap(nextMapId);
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getCrossingDuration() {
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
	
	public void reverse() {
		Collections.reverse(nodes);
	}
	
	private class SimplePathNode extends PathNode {

		protected SimplePathNode(int id, int direction) {
			super(id, direction);
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
	}
}