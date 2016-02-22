package game.d2o.modules;

import game.d2o.GameData;
import game.d2o.GameDataFileAccessor;

import java.awt.Point;
import java.util.Arrays;
import java.util.Vector;

public class SubArea {
    public static final String MODULE = "SubAreas";
    private static SubArea[] _allSubAreas;

    //private Rectangle _bounds;
    public int id;
    public int nameId;
    public int areaId;
    public Vector<AmbientSound> ambientSounds;
    public Vector<Vector<Integer>> playlists;
    public Vector<Integer> mapIds;
    public Rectangle bounds;
    public Vector<Integer> shape;
    public Vector<Integer> customWorldMap;
    public int packId;
    public int level;
    public boolean isConquestVillage;
    public boolean basicAccountAllowed;
    public boolean displayOnWorldMap;
    public Vector<Integer> monsters;
    public Vector<Integer> entranceMapIds;
    public Vector<Integer> exitMapIds;
    public boolean capturable;
    //private String _name;
    private Area _area;
    private WorldMap _worldMap;
    private Point _center;
    
    static {
    	GameDataFileAccessor.getInstance().init(MODULE);
    }
    
    public static SubArea getSubAreaById(int id) {
    	SubArea sa = (SubArea) GameData.getObject(MODULE, id);
    	if(sa == null || sa.getArea() == null)
    		return null;
    	return sa;
    }
    
    public static SubArea getSubAreaByMapId(int id) {
    	MapPosition mp = MapPosition.getMapPositionById(id);
    	if(mp != null)
    		return mp.getSubArea();
    	return null;
    }
    
    public static SubArea[] getAllSubArea() {
    	if(_allSubAreas != null)
    		return _allSubAreas;
    	Object[] objArray = GameData.getObjects(MODULE);
    	_allSubAreas = Arrays.copyOf(objArray, objArray.length, SubArea[].class);
    	return _allSubAreas;
    }
    
    /*
    public String getName() {
    	if(this._name == null)
    		this._name = I18n.getText(this.nameId);
    	return this._name;
    }
    */
    
    public Area getArea() {
    	if(this._area == null)
    		this._area = Area.getAreaById(this.areaId);
    	return this._area;
    }
    
    public WorldMap getWorldMap() {
    	if(this._worldMap == null) {
    		if(this.hasCustomWorldMap())
    			this._worldMap = WorldMap.getWorldMapById(this.customWorldMap.get(0));
    		else
    			this._worldMap = this.getArea().getWorldMap();
    	}
    	return this._worldMap;
    }
    
    public boolean hasCustomWorldMap() {
        return this.customWorldMap != null && (this.customWorldMap.size() > 0);
    }
    
    public Point getCenter() {
    	int shapeSize = this.shape.size();
    	if(this._center == null && shapeSize > 0) {
    		int i1 = this.shape.get(0);
    		int i2 = this.shape.get(1);
    		int i3 = i1 > 10000 ? i1 - 11000 : i1;
    		int i4 = i3;
    		int i5 = i2 > 10000 ? i2 - 11000 : i2;
    		int i6 = i5;
    		for(int i = 2; i < shapeSize; i += 2) {
    			i1 = this.shape.get(i);
    			i2 = this.shape.get(i + 1);
    			if(i1 > 10000)
    				i1 -= 11000;
    			if(i2 > 10000)
    				i2 -= 11000;
    			i4 = i1 < i4 ? i1 : i4;
    			i3 = i1 > i3 ? i1 : i3;
    			i6 = i2 < i6 ? i2 : i6;
    			i5 = i2 > i5 ? i2 : i5;
    		}
    		this._center = new Point((i4 + i3) / 2, (i6 + i5) / 2);
    	}
    	return this._center;
    }
}