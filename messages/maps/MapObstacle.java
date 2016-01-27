package messages.maps;

import utilities.ByteArray;

public class MapObstacle {
    public int obstacleCellId = 0;
    public int state = 0;
	
	public MapObstacle(ByteArray buffer) {
        this.obstacleCellId = buffer.readVarShort();
        this.state = buffer.readByte();
	}
}