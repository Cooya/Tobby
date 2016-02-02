package roleplay.paths;

import java.util.Vector;

public class Path {
	private Vector<PathNode> nodes;
	private int currentPos;
	private boolean isLoop;
	
	protected Path(boolean isLoop) {
		this.nodes = new Vector<PathNode>();
		this.currentPos = 0;
		this.isLoop = isLoop;
	}
	
	protected void addNode(int mapId, int direction) {
		this.nodes.add(new PathNode(mapId, direction));
	}
	
	protected int getCurrentMapId() {
		return this.nodes.get(this.currentPos).getMapId();
	}
	
	protected int nextMap() {
		if(this.currentPos == this.nodes.size() - 1)
			if(this.isLoop)
				this.currentPos = 0;
			else
				return -1;
		else
			this.currentPos++;
		return this.nodes.get(this.currentPos).getDirection();
	}
	
	protected void resetPath() {
		this.currentPos = 0;
	}
}
