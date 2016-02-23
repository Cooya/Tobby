package messages.exchange;

import messages.Message;
import utilities.ByteArray;

public class ObjectDeletedMessage extends Message {
	public int objectUID = 0;

	public ObjectDeletedMessage(Message msg) {
		super(msg);
		deserialize();
	}

	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.objectUID = buffer.readVarInt();
	}
}