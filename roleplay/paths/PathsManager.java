package roleplay.paths;

import java.util.Hashtable;

import roleplay.movement.Pathfinder;

public class PathsManager {
	private static Hashtable<String, Path> paths;
	
	public static void createPath(String name, boolean isLoop) {
		paths.put(name, new Path(isLoop));
	}
	
	public static void addNodeToPath(String name, int mapId, int direction) {
		if(direction != Pathfinder.LEFT || direction != Pathfinder.RIGHT || direction != Pathfinder.UP || direction != Pathfinder.DOWN)
			throw new Error("Invalid direction for paths");
		Path path = checkPathName(name);
		path.addNode(mapId, direction);
	}
	
	public static int nextMap(String name) {
		Path path = checkPathName(name);
		return path.nextMap();
	}
	
	public static void resetPath(String name) {
		Path path = checkPathName(name);
		path.resetPath();
	}
	
	public static int getCurrentMapId(String name) {
		Path path = paths.get(name);
		return path.getCurrentMapId();
	}
	
	private static Path checkPathName(String name) {
		Path path = paths.get(name);
		if(path == null)
			throw new Error("Unknown path name.");
		return path;
	}
}
