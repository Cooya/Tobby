package messages.inventory;

import gamedata.inventory.ObjectItem;

import messages.NetworkMessage;

public class ObjectsAddedMessage extends NetworkMessage {
	public ObjectItem[] object;

	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		int nb = this.content.readShort();
		this.object = new ObjectItem[nb];
		for(int i = 0; i < nb; ++i)
			this.object[i] = new ObjectItem(this.content);
	}
}