package roleplay.d2o.modules;

import java.util.Vector;

import roleplay.d2o.GameData;
import roleplay.d2o.GameDataFileAccessor;

public class MapCoordinates {
	public static final String MODULE = "MapCoordinates";
    private static final int UNDEFINED_COORD = Integer.MIN_VALUE; //-2147483648
    
    static {
    	GameDataFileAccessor.getInstance().init(MODULE);
    }
    
    public int compressedCoords;
    public Vector<Integer> mapIds;
    private int _x = -2147483648;
    private int _y = -2147483648;
    private Vector<MapPosition> _maps;
    
    public static MapCoordinates getMapCoordinatesByCompressedCoords(int i) {
    	return (MapCoordinates) GameData.getObject(MODULE, i);
    }
    
    public static MapCoordinates getMapCoordinatesByCoords(int i1, int i2) {
    	return getMapCoordinatesByCompressedCoords((getCompressedValue(i1) << 16) + getCompressedValue(i2));
    }
    
    private static int getSignedValue(int i) {
    	int i2 = i & 32767;
    	return (i & 0x8000) > 0 ? -i2 : i2;
    }
    
    private static int getCompressedValue(int i) {
    	return i < 0 ? (0x8000 | (i & 32767)) : i & 32767;
    }
    
    public int getX() {
    	if(this._x == UNDEFINED_COORD)
    		this._x = getSignedValue((this.compressedCoords & 0xFFFF0000) >> 16);
    	return this._x;
    }
    
    public int getY() {
    	if(this._y == UNDEFINED_COORD)
    		this._y = getSignedValue(this.compressedCoords & 0xFFFF);
    	return this._y;
    }
    
    public Vector<MapPosition> getMaps() {
    	if(this._maps == null) {
    		this._maps = new Vector<MapPosition>(this.mapIds.size());
    		for(int mapId : this.mapIds)
    			this._maps.add(MapPosition.getMapPositionById(mapId));
    	}
    	return this._maps;
    }
}