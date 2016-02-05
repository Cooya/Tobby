package messages.context;

import utilities.ByteArray;
import messages.Message;

public class ChangeMapMessage extends Message {
	public int mapId = 0;
	
	public ChangeMapMessage() {
		super();
	}
	
	public void serialize(int mapId) {
		this.mapId = mapId;
		
		ByteArray buffer = new ByteArray();
		buffer.writeInt(mapId);
		completeInfos(buffer);
	}
}
