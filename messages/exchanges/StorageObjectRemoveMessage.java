package messages.exchanges;

import messages.NetworkMessage;

public class StorageObjectRemoveMessage extends NetworkMessage {
	public int objectUID = 0;

	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		this.objectUID = this.content.readVarInt();
	}
}