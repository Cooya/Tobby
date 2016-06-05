package controller.pathfinding;

import gamedata.d2o.modules.MapPosition;

import java.util.Vector;

import main.FatalError;
import main.Log;

// cache pas encore utilisé
class PathsCache {
	private static Vector<Path> cache = new Vector<Path>();
	private static final int MAX_ENTRIES = 100;
	private static final boolean DEBUG = false;
	
	/*
	public synchronized static Path buildPath(int x1, int y1, int x2, int y2, int currentCellId) {
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
	*/

	protected static Path toMap(int targetMapId, int sourceMapId, int startCellId) {
		Pathfinder pf = new MapsPathfinder(startCellId);
		Path path = pf.compute(sourceMapId, targetMapId);
		if(path == null)
			throw new FatalError("Impossible to find a path between the map with id = " + sourceMapId + " and the map with id = " + targetMapId + ".");
		path.startCellId = startCellId;
		return path;
	}
	
	protected static Path toArea(int areaId, int sourceMapId, int startCellId) {
		if(DEBUG)
			Log.info("Going to area with id = " + areaId + " from  " + MapPosition.getMapPositionById(sourceMapId) + ".");
		MapPosition[] mapPositions = MapPosition.getMapPositions();
		Vector<MapPosition> mapPositionsInArea = new Vector<MapPosition>();
		for(MapPosition mapPosition : mapPositions)
			if(mapPosition.subAreaId == areaId)
				mapPositionsInArea.add(mapPosition);
		if(mapPositionsInArea.size() == 0)
			throw new FatalError("Invalid area id.");
		if(DEBUG)
			Log.info(mapPositionsInArea.size() + " maps in the area with id = " + areaId + ".");
		Pathfinder pathfinder = new MapsPathfinder(startCellId);
		Path bestPath = null;
		Path tmpPath;
		int shortestDistance = 999999;
		int tmpDistance;
		for(MapPosition mapPosition : mapPositionsInArea) {
			if(mapPosition.worldMap < 1)
				continue;
			tmpPath = pathfinder.compute(sourceMapId, mapPosition.id);
			if(tmpPath == null) // chemin impossible
				continue;
			tmpDistance = tmpPath.getCrossingDuration(); // c'est en fait la distance
			if(tmpDistance < shortestDistance) {
				shortestDistance = tmpDistance;
				bestPath = tmpPath;
			}
		}
		if(bestPath != null)
			bestPath.startCellId = startCellId;
		return bestPath;
	}

	
	@SuppressWarnings("unused")
	private static void addPath(Path path) {
		cache.add(path);
		if(cache.size() > MAX_ENTRIES)
			cache.remove(0);
	}
	
	@SuppressWarnings("unused")
	private static int selectBestMapId(int x, int y) {
		Vector<MapPosition> vector = MapPosition.getMapPositionByCoord(x, y);
		int vectorSize = vector.size();
		if(vectorSize == 0)
			throw new FatalError("Invalid map coords : [" + x + ", " + y +"].");
		if(vectorSize == 1)
			return vector.get(0).id;
		for(MapPosition mp : vector)
			if(mp.worldMap > 0)
				return mp.id;
		throw new FatalError("An error to fix, Nico !");
	}
}