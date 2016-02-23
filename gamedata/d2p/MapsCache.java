package gamedata.d2p;

import gamedata.d2p.ankama.Map;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import utilities.ByteArray;

public class MapsCache {
	private static LinkedHashMap<Integer, Map> cache = new LinkedHashMap<Integer, Map>();
	private static final int MAX_ENTRIES = 100;
	
	public static Map loadMap(int mapId) {
		Map map = cache.get(mapId);
		if(map != null)
			return map;
		ByteArray raw = D2pReader.getBinaryMap(mapId);
		if(raw == null)
			return null;
		map = new Map(raw);
		cache.put(map.id, map);
		return map;	
	}

    protected boolean removeEldestEntry(Entry<Integer, Map> eldest) {
       return cache.size() > MAX_ENTRIES;
    }
}