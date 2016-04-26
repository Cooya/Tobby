package messages.exchanges;

import messages.Message;

public class ObjectDeletedMessage extends Message {
	public int objectUID = 0;

	@Override
	public void serialize() {
		// not implemented yet
	}

	@Override
	public void deserialize() {
		this.objectUID = this.content.readVarInt();
	}
}