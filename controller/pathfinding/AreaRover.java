package controller.pathfinding;

import gamedata.d2p.MapsCache;
import gamedata.d2p.ankama.Map;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import controller.CharacterController;
import main.Instance;

public class AreaRover {
	private int areaId;
	
	public AreaRover(int areaId, CharacterController CC) {
		this.areaId = areaId;
		if(CC.infos.currentMap.subareaId != this.areaId) {
			Instance.log("Going to area with id = " + this.areaId + ".");
			Path.getPathToArea(areaId, CC.infos).run(CC);
		}
	}

	public int nextMap(CharacterController CC) {
		if(CC.infos.currentMap.subareaId != this.areaId) {
			Instance.log("Going to area with id = " + this.areaId + ".");
			Path.getPathToArea(areaId, CC.infos).run(CC);
		}
		Hashtable<Integer, Integer> neighbours  = new Hashtable<Integer, Integer>();
		for(int direction = 0; direction < 8; direction += 2)
			neighbours.put(direction, CC.infos.currentMap.getNeighbourMapFromDirection(direction));
		
		Random randomGen = new Random();
		int randomDirection;
		Map map;
		while(true) {
			List<Integer> directionList = Collections.list(neighbours.keys());
			randomDirection = directionList.get(randomGen.nextInt(neighbours.size())); // on prend une direction au hasard
			map = MapsCache.loadMap(neighbours.get(randomDirection));
			if(map != null && map.subareaId == CC.infos.currentMap.subareaId)
				return randomDirection;
			else {
				Instance.log("Direction to " + Pathfinder.directionToString(randomDirection) + " impossible.");
				neighbours.remove(randomDirection);
			}
		}
	}
}