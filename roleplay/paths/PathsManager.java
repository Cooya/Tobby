package roleplay.paths;

import java.util.Hashtable;

import utilities.Log;

public class PathsManager {
	private static Hashtable<String, Path> paths = new Hashtable<String, Path>(); 
	
	public static Path createPath(String name, boolean isLoop) {
		Log.p("Creation of a path named \"" + name + "\".");
		Path path = new Path(name, isLoop);
		paths.put(name, path);
		return path;
	}
	
	public static Path getPathByName(String name) {
		return paths.get(name);
	}
	
	public static void removePath(String name) {
		paths.remove(name);
	}
}