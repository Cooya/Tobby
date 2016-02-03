package roleplay.paths;

import java.util.Vector;

import roleplay.movement.Pathfinder;

public class Path {
	private Vector<PathNode> nodes;
	private int currentPos;
	private boolean isLoop;
	
	public Path(boolean isLoop) {
		this.nodes = new Vector<PathNode>();
		this.currentPos = 0;
		this.isLoop = isLoop;
	}
	
	public void addNode(int mapId, int direction) {
		if(direction != Pathfinder.LEFT && direction != Pathfinder.RIGHT && direction != Pathfinder.UP && direction != Pathfinder.DOWN)
			throw new Error("Invalid direction for create a node path.");
		this.nodes.add(new PathNode(mapId, direction));
	}
	
	public int nextMap() {
		if(this.currentPos == this.nodes.size())
			if(this.isLoop)
				this.currentPos = 0;
			else
				return -1;
		
		return this.nodes.get(this.currentPos++).getDirection();
	}
	
	public void resetPositionPath() {
		this.currentPos = 0;
	}
	
	public void checkCurrentPos(int currentMapId) {
		int vectorSize = nodes.size();
		for(int i = 0; i < vectorSize; ++i)
			if(nodes.get(i).getMapId() == currentMapId) {
				this.currentPos = i;
				return;
			}
		throw new Error("Impossible to run this path, invalid character position.");
	}
}
