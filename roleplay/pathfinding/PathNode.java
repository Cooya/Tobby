package roleplay.pathfinding;

public abstract class PathNode {
	protected int id;
	protected PathNode parent;
	protected double cost;
	protected int direction; // provenance
	
	protected PathNode(int id, int direction, PathNode parent) {
		this.id = id;
		this.parent = parent;
		this.direction = direction;
	}
	
	protected PathNode(int id, int direction) {
		this(id, direction, null);
	}

	protected abstract boolean equals(PathNode node);
	protected abstract double distanceTo(PathNode node);
	protected abstract boolean isAccessible();
	protected abstract int getCrossingDuration(boolean mode);
	public abstract String toString();
}