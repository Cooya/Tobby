package messages.context;

import utilities.ByteArray;
import messages.Message;

public class GameContextReadyMessage extends Message {
	public int mapId = 0;

	public GameContextReadyMessage() {
		super();
	}
	
	public void serialize(int mapId) {
		this.mapId = mapId;
		
		ByteArray buffer = new ByteArray();
		buffer.writeInt(this.mapId);
		super.completeInfos(buffer);
	}
}