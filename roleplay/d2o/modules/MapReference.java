package roleplay.d2o.modules;

import roleplay.d2o.GameData;

public class MapReference {
    public static final String MODULE = "MapReferences";

    public int id;
    public int mapId;
    public int cellId;
    
    public static MapReference getMapReferenceById(int id) {
    	return (MapReference) GameData.getObject(MODULE, id);
    }
    
    public static MapReference[] getAllMapReference() {
    	return (MapReference[]) GameData.getObjects(MODULE);
    }
}