package game.d2o.modules;

import game.d2o.GameData;
import game.d2o.GameDataFileAccessor;

import java.util.Arrays;

public class MapReference {
    public static final String MODULE = "MapReferences";
    
    static {
    	GameDataFileAccessor.getInstance().init(MODULE);
    }

    public int id;
    public int mapId;
    public int cellId;
    
    public static MapReference getMapReferenceById(int id) {
    	return (MapReference) GameData.getObject(MODULE, id);
    }
    
    public static MapReference[] getAllMapReference() {
    	Object[] objArray = GameData.getObjects(MODULE);
        return Arrays.copyOf(objArray, objArray.length, MapReference[].class);
    }
}