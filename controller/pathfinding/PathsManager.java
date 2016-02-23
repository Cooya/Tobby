package controller.pathfinding;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Hashtable;

import main.Instance;

@SuppressWarnings("resource")
public class PathsManager {
	private static final String pathsFilePath = "Ressources/paths.txt";
	private static Hashtable<String, Path> paths = new Hashtable<String, Path>(); 
	
	static {
		BufferedReader buffer = null;
		try {
			Instance.log("Loading paths from paths file...");
			buffer = new BufferedReader(new FileReader(pathsFilePath));
			Path path;
			boolean isLoop;
			int direction;
			String[] splitLine;
			String line;
			do {
				line = buffer.readLine().trim();
				splitLine = line.split(":");
				
				if(splitLine.length != 2)
					throw new Error("Malformated paths file.");
				if(splitLine[1].equals("true"))
					isLoop = true;
				else if(splitLine[1].equals("false"))
					isLoop = false;
				else
					throw new Error("Malformated paths file.");
				
				path = new Path(splitLine[0], isLoop);
				paths.put(splitLine[0], path);
				while((line = buffer.readLine()) != null && !line.equals("")) {
					line = line.trim();
					splitLine = line.split("/");
					
					if(splitLine.length != 2)
						throw new Error("Malformated paths file.");
					if(splitLine[1].equals("l"))
						direction = Pathfinder.LEFT;
					else if(splitLine[1].equals("r"))
						direction = Pathfinder.RIGHT;
					else if(splitLine[1].equals("u"))
						direction = Pathfinder.UP;
					else if(splitLine[1].equals("d"))
						direction = Pathfinder.DOWN;
					else
						throw new Error("Malformated paths file.");
					
					path.addNode(Integer.parseInt(splitLine[0]), direction);
				}
			} while(line != null);
			buffer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Path createPath(String name, boolean isLoop) {
		Instance.log("Creation of a path named \"" + name + "\".");
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