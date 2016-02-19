package game.pathfinding;

import game.d2p.MapsCache;
import game.d2p.ankama.Map;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import main.CharacterController;
import main.FatalError;
import utilities.Log;

public class AreaRover {
	private int areaId;
	
	public AreaRover(int areaId) {
		this.areaId = areaId;
	}
	
	public AreaRover(CharacterController CC) {
		this.areaId = CC.infos.currentMap.subareaId;
	}
	
	public int nextMap(CharacterController CC) {
		if(CC.infos.currentMap.subareaId != this.areaId)
			new FatalError("Character is not in the good area.");
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
				Log.p("Direction to " + Pathfinder.directionToString(randomDirection) + " impossible.");
				neighbours.remove(randomDirection);
			}
		}
	}
}