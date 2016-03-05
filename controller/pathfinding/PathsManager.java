package controller.pathfinding;

import gamedata.d2p.ankama.Map;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Hashtable;

import main.FatalError;
import main.Instance;

// classe inutilisée

@SuppressWarnings("resource")
class PathsManager {
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
					throw new FatalError("Malformated paths file.");
				if(splitLine[1].equals("true"))
					isLoop = true;
				else if(splitLine[1].equals("false"))
					isLoop = false;
				else
					throw new FatalError("Malformated paths file.");
				
				path = new Path(splitLine[0], isLoop);
				paths.put(splitLine[0], path);
				while((line = buffer.readLine()) != null && !line.equals("")) {
					line = line.trim();
					splitLine = line.split("/");
					
					if(splitLine.length != 2)
						throw new FatalError("Malformated paths file.");
					if(splitLine[1].equals("l"))
						direction = Map.LEFT;
					else if(splitLine[1].equals("r"))
						direction = Map.RIGHT;
					else if(splitLine[1].equals("u"))
						direction = Map.UP;
					else if(splitLine[1].equals("d"))
						direction = Map.DOWN;
					else
						throw new FatalError("Malformated paths file.");
					
					path.addNode(Integer.parseInt(splitLine[0]), direction);
				}
			} while(line != null);
			buffer.close();
		} catch(Exception e) {
			throw new FatalError(e);
		}
	}
	
	protected synchronized static Path createPath(String name, boolean isLoop) {
		Instance.log("Creation of a path named \"" + name + "\".");
		Path path = new Path(name, isLoop);
		paths.put(name, path);
		return path;
	}
	
	protected synchronized static Path getPathByName(String name) {
		return paths.get(name);
	}
	
	protected synchronized static void removePath(String name) {
		paths.remove(name);
	}
}