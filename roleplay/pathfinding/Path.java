package roleplay.pathfinding;

import java.util.Collections;
import java.util.Vector;

import roleplay.d2o.modules.MapPosition;
import roleplay.pathfinding.Pathfinder.PathNode;
import main.CharacterController;
import utilities.Log;

public class Path {
	private String name;
	private Vector<PathNode> nodes;
	private int currentPos;
	private boolean isLoop;
	
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
	
	public static void moveTo(int mapId, CharacterController CC) {
		Pathfinder pf = new MapsPathfinder(CC.getCurrentCellId());
		Path path = pf.compute(CC.getCurrentMapId(), mapId);
		path.run(CC);
	}
	
	public static void moveTo(int x, int y, CharacterController CC) {
		moveTo(selectBestMapId(x, y), CC);
	}
	
	public static Path buildPath(int x1, int y1, int x2, int y2, int currentCellId) {
		Pathfinder pf = new MapsPathfinder(currentCellId);
		return pf.compute(selectBestMapId(x1, y1), selectBestMapId(x2, y2));
	}
	
	public static Path buildPath(int srcMapId, int destMapId, int currentCellId) {
		Pathfinder pf = new MapsPathfinder(currentCellId);
		return pf.compute(srcMapId, destMapId);
	}
	
	public void resetPositionPath() {
		this.currentPos = 0;
	}
	
	public void run(CharacterController CC) {
		Log.p("Running path named \"" + name + "\".");
		
		if(!checkCurrentPos(CC.getCurrentMapId())) // si le perso n'est pas sur le trajet
			moveTo(nodes.get(0).id, CC);

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
	
	private boolean checkCurrentPos(int currentMapId) {
		int vectorSize = nodes.size();
		for(int i = 0; i < vectorSize; ++i)
			if(nodes.get(i).id == currentMapId) {
				this.currentPos = i;
				return true;
			}
		return false;
	}

	private static int selectBestMapId(int x, int y) {
		Vector<MapPosition> vector = MapPosition.getMapPositionByCoord(x, y);
		int vectorSize = vector.size();
		if(vectorSize == 0)
			throw new Error("Invalid map coords : [" + x + ", " + y +"].");
		if(vectorSize == 1)
			return vector.get(0).id;
		for(MapPosition mp : vector)
			if(mp.worldMap == 1)
				return mp.id;
		throw new Error("An error to fix, Nico !");
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