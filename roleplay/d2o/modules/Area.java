package roleplay.d2o.modules;

import java.awt.Rectangle;

import roleplay.d2o.GameData;

public class Area {
	public static final String MODULE = "Areas";
    private static Area[] _allAreas;

    public int id;
    public int nameId;
    public int superAreaId;
    public boolean containHouses;
    public boolean containPaddocks;
    public Rectangle bounds;
    public int worldmapId;
    public boolean hasWorldMap;
    //private String _name;
    private SuperArea _superArea;
    private boolean _hasVisibleSubAreas;
    private boolean _hasVisibleSubAreasInitialized;
    private WorldMap _worldMap;
    
    public static Area getAreaById(int id) {
    	Area area = (Area) GameData.getObject(MODULE, id);
    	if(area == null || area.getSuperArea() == null || !area.hasVisibleSubAreas())
    		return null;
    	return area;
    }
    
    public static Area[] getAllArea() {
    	if(_allAreas != null)
    		return _allAreas;
    	_allAreas = (Area[]) GameData.getObjects(MODULE);
    	return _allAreas;
    }
    
    /*
    public String getName() {
    	if(this._name == null)
    		this._name = I18n.getText(this.nameId);
    	return this._name;
    }
    */
    
    public SuperArea getSuperArea() {
    	if(this._superArea == null)
    		this._superArea = SuperArea.getSuperAreaById(this.superAreaId);
    	return this._superArea;
    }
    
    public boolean hasVisibleSubAreas() {
    	if(!this._hasVisibleSubAreasInitialized) {
    		this._hasVisibleSubAreas = true;
    		this._hasVisibleSubAreasInitialized = true;
    	}
    	return this._hasVisibleSubAreas;
    }
    
    public WorldMap getWorldMap() {
    	if(this._worldMap == null) {
    		if(!hasWorldMap)
    			this._worldMap = getSuperArea().getWorldMap();
    		else
    			this._worldMap = WorldMap.getWorldMapById(this.worldmapId);
    	}
    	return this._worldMap;
    }
}