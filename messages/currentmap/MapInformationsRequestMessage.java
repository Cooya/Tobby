package messages.currentmap;

import messages.Message;
import utilities.ByteArray;

public class MapInformationsRequestMessage extends Message {
	private int mapId;

	public MapInformationsRequestMessage() {
		super();
	}

	public void serialize(int mapId) {
		this.mapId = mapId;
		ByteArray buffer = new ByteArray();
		buffer.writeInt(this.mapId);
		
		completeInfos(buffer);
	}
}