package controller.pathfinding;

import gamedata.d2o.modules.MapPosition;

import java.util.Collections;
import java.util.Vector;

import controller.CharacterController;
import controller.MovementAPI;
import controller.informations.CharacterInformations;
import controller.pathfinding.Pathfinder.PathNode;
import main.Instance;

public class Path {
	private String name;
	private Vector<PathNode> nodes;
	private int currentPos;
	private boolean isLoop;
	protected int startCellId = -1; // uniquement pour les paths de maps
	
	public static Path getPathToArea(int areaId, CharacterInformations infos) {
		MapPosition[] mapPositions = MapPosition.getMapPositions();
		Vector<MapPosition> mapPositionsInArea = new Vector<MapPosition>();
		for(MapPosition mapPosition : mapPositions)
			if(mapPosition.subAreaId == areaId)
				mapPositionsInArea.add(mapPosition);
		if(mapPositionsInArea.size() == 0)
			throw new Error("Invalid area id.");
		Instance.log(mapPositionsInArea.size() + " maps in the area with id = " + areaId + ".");
		Pathfinder pathfinder = new MapsPathfinder(infos.currentCellId);
		Path bestPath = null;
		Path tmpPath;
		int shortestDistance = 999999;
		int tmpDistance;
		for(MapPosition mapPosition : mapPositionsInArea) {
			if(mapPosition.worldMap < 1)
				continue;
			tmpPath = pathfinder.compute(infos.currentMap.id, mapPosition.id);
			if(tmpPath == null)
				continue;
			tmpDistance = tmpPath.getCrossingDuration(); // c'est en fait la distance
			if(tmpDistance < shortestDistance) {
				shortestDistance = tmpDistance;
				bestPath = tmpPath;
			}
		}
		return bestPath;
	}
	
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
		Instance.log("Running path named \"" + name + "\".");

		CC.currentPathName = this.name;
		int nextMapId;
		while((nextMapId = nextMap()) != -1)
			MovementAPI.changeMap(nextMapId, CC);
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

		protected int getCrossingDuration(boolean mode) {
			throw new Error("Phony method !");
		}
		
		public String toString() {
			return String.valueOf(this.id);
		}
	}
}