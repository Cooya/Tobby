package messages.inventory;

import messages.NetworkMessage;

public class ObjectsDeletedMessage extends NetworkMessage {
	public int[] objectUID;

	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		int nb = this.content.readShort();
		this.objectUID = new int[nb];
		for(int i = 0; i < nb; ++i)
			this.objectUID[i] = this.content.readVarInt();
	}
}