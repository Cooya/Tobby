package roleplay.d2o.modules;

import roleplay.d2o.GameData;

public class SuperArea {
	public static final String MODULE = "SuperAreas";
    private static SuperArea[] _allSuperAreas;

    public int id;
    public int nameId;
    public int worldmapId;
    public boolean hasWorldMap;
    //private String _name;
    private WorldMap _worldmap;
    
    public static SuperArea getSuperAreaById(int id) {
    	return (SuperArea) GameData.getObject(MODULE, id);
    }
    
    public static SuperArea[] getAllSuperArea() {
    	if(_allSuperAreas != null)
    		return _allSuperAreas;
    	_allSuperAreas = (SuperArea[]) GameData.getObjects(MODULE);
    	return _allSuperAreas;
    }
    
    /*
    public String getName() {
    	if(this._name == null)
    		this._name = I18n.getText(this.nameId);
    	return this._name;
    }
    */
    
    public WorldMap getWorldMap() {
    	if(this._worldmap == null) {
    		if(!this.hasWorldMap)
    			this.worldmapId = 1;
    		this._worldmap = WorldMap.getWorldMapById(this.worldmapId);
    	}
    	return this._worldmap;	
    }
}