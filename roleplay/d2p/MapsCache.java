package roleplay.d2p;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import roleplay.d2p.ankama.Map;

public class MapsCache {
	private static LinkedHashMap<Integer, Map> cache = new LinkedHashMap<Integer, Map>();
	private static final int MAX_ENTRIES = 100;
	
	public static Map loadMap(int mapId) {
		Map map = cache.get(mapId);
		if(map != null)
			return map;
		map = new Map(D2pReader.getBinaryMap(mapId));
		cache.put(map.id, map);
		return map;	
	}

    protected boolean removeEldestEntry(Entry<Integer, Map> eldest) {
       return cache.size() > MAX_ENTRIES;
    }
}