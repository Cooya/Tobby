package messages.exchanges;

import gamedata.inventory.ObjectItem;

import java.util.Vector;

import messages.NetworkMessage;

public class StorageObjectsUpdateMessage extends NetworkMessage {
	public Vector<ObjectItem> objectList;

	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		int nb = this.content.readShort();
		this.objectList = new Vector<ObjectItem>(nb);
		for(int i = 0; i < nb; ++i)
			this.objectList.add(new ObjectItem(this.content));
	}
}