package gamedata.maps;

import utilities.ByteArray;

public class MapCoordinates {
    public int worldX = 0;
    public int worldY = 0;
    
    public MapCoordinates(ByteArray buffer) {
    	this.worldX = buffer.readShort();
    	this.worldY = buffer.readShort();
    }
}