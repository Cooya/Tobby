package messages.inventory;

import gamedata.inventory.ObjectItem;

import java.util.Vector;

import messages.NetworkMessage;

public class ObjectsAddedMessage extends NetworkMessage {
	public Vector<ObjectItem> object;

	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		int nb = this.content.readShort();
		this.object = new Vector<ObjectItem>(nb);
		for(int i = 0; i < nb; ++i)
			this.object.add(new ObjectItem(this.content));
	}
}