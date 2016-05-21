package messages.inventory;

import messages.NetworkMessage;

public class ObjectDeletedMessage extends NetworkMessage {
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