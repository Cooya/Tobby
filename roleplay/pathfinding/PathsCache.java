package roleplay.pathfinding;

import java.util.Vector;

import main.CharacterController;
import roleplay.d2o.modules.MapPosition;

public class PathsCache {
	private static Vector<Path> cache = new Vector<Path>();
	private static final int MAX_ENTRIES = 100;
	
	public static Path buildPath(int x1, int y1, int x2, int y2, int currentCellId) {
		int srcMapId = selectBestMapId(x1, y1);
		int destMapId = selectBestMapId(x2, y2);
		MapsPathfinder mpf = new MapsPathfinder(currentCellId);
		for(Path path : cache)
			if(path.getFirstNode().id == srcMapId && path.getLastNode().id == destMapId && mpf.inSameZone(srcMapId, currentCellId, path.startCellId))
				return path;
		Path path = mpf.compute(srcMapId, destMapId);
		path.startCellId = currentCellId;
		addPath(path);
		return path;
	}
	
	public static void moveTo(int mapId, CharacterController CC) {
		Pathfinder pf = new MapsPathfinder(CC.currentCellId);
		Path path = pf.compute(CC.currentMap.id, mapId);
		path.startCellId = CC.currentCellId;
		path.run(CC);
	}
	
	public static void moveTo(int x, int y, CharacterController CC) {
		moveTo(selectBestMapId(x, y), CC);
	}
	
	private static void addPath(Path path) {
		cache.add(path);
		if(cache.size() > MAX_ENTRIES)
			cache.remove(0);
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
}