package messages.exchanges;

import messages.Message;
import utilities.ByteArray;

public class ObjectQuantityMessage extends Message {
	public int objectUID = 0;
	public int quantity = 0;

	public ObjectQuantityMessage(Message msg) {
		super(msg);
		deserialize();
	}

	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.objectUID = buffer.readVarInt();
		this.quantity = buffer.readVarInt();
	}
}