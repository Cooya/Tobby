package roleplay.d2o.modules;

import roleplay.d2o.GameData;

public class AmbientSound extends PlaylistSound {
    public static final int AMBIENT_TYPE_ROLEPLAY = 1;
    public static final int AMBIENT_TYPE_AMBIENT = 2;
    public static final int AMBIENT_TYPE_FIGHT = 3;
    public static final int AMBIENT_TYPE_BOSS = 4;
    public static final String MODULE = "AmbientSounds";

    public int criterionId;
    public int silenceMin;
    public int silenceMax;
    public int type_id;
    
    public static AmbientSound getAmbientSoundById(int id) {
    	return (AmbientSound) GameData.getObject(MODULE, id);
    }
}