package roleplay.d2o.modules;

import java.util.Arrays;

import roleplay.d2o.GameData;
import roleplay.d2o.GameDataFileAccessor;

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