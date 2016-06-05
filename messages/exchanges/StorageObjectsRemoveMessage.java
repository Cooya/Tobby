package messages.exchanges;

import messages.NetworkMessage;

public class StorageObjectsRemoveMessage extends NetworkMessage {
	public int[] objectUIDList;

	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		int nb = this.content.readShort();
		this.objectUIDList = new int[nb];
		for(int i = 0; i < nb; ++i)
			this.objectUIDList[i] = this.content.readVarInt();
	}
}