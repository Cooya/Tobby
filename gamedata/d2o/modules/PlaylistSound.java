package gamedata.d2o.modules;

import gamedata.d2o.GameData;

// ce n'est pas un fichier d2o
public class PlaylistSound {
    public static final String MODULE = "PlaylistSounds";
    
    public String id;
    public int volume;
    public int channel = 0;
    
    public static PlaylistSound getPlaylistSoundById(int id) {
    	return (PlaylistSound) GameData.getObject(MODULE, id);
    }
}