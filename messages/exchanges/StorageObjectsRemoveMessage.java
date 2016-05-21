package messages.exchanges;

import java.util.Vector;

import messages.NetworkMessage;

public class StorageObjectsRemoveMessage extends NetworkMessage {
	public Vector<Integer> objectUIDList;

	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		int nb = this.content.readShort();
		this.objectUIDList = new Vector<Integer>(nb);
		for(int i = 0; i < nb; ++i)
			this.objectUIDList.add(this.content.readVarInt());
	}
}