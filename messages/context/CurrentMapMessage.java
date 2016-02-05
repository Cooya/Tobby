package messages.context;

import messages.Message;
import utilities.ByteArray;

public class CurrentMapMessage extends Message {
    public int mapId;
	public String mapKey;

	public CurrentMapMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.mapId = buffer.readInt();
		this.mapKey = buffer.readUTF();
	}
}
