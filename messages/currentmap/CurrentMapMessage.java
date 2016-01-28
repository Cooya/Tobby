package messages.currentmap;

import messages.Message;
import utilities.ByteArray;

public class CurrentMapMessage extends Message {
    private int mapId;
	private String mapKey;

	public CurrentMapMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.mapId = buffer.readInt();
		this.mapKey = buffer.readUTF();
	}

	public int getMapId() {
		return this.mapId;
	}
	
	public String getMapKey() {
		return this.mapKey;
	}
}
