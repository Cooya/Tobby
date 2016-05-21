package messages.inventory;

import java.util.Vector;

import messages.NetworkMessage;

public class ObjectsDeletedMessage extends NetworkMessage {
	public Vector<Integer> objectUID;

	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		int nb = this.content.readShort();
		this.objectUID = new Vector<Integer>(nb);
		for(int i = 0; i < nb; ++i)
			this.objectUID.add(this.content.readVarInt());
	}
}