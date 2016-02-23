package gamedata.d2o.modules;

import gamedata.d2o.GameData;

public class PlaylistSound {
    public static final String MODULE = "PlaylistSounds";

    public int id;
    public int volume;
    public int channel = 0;
    
    public static PlaylistSound getPlaylistSoundById(int id) {
    	return (PlaylistSound) GameData.getObject(MODULE, id);
    }
}