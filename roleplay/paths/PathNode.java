package roleplay.paths;

public class PathNode {
	private int mapId;
	private int direction;
	
	protected PathNode(int mapId, int direction) {
		this.mapId = mapId;
		this.direction = direction;
	}
	
	protected int getMapId() {
		return this.mapId;
	}
	
	protected int getDirection() {
		return this.direction;
	}
}