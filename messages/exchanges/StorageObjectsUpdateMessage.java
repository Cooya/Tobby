package messages.exchanges;

import gamedata.inventory.ObjectItem;

import messages.NetworkMessage;

public class StorageObjectsUpdateMessage extends NetworkMessage {
	public ObjectItem[] objectList;

	@Override
	public void serialize() {
		
	}
	
	@Override
	public void deserialize() {
		int nb = this.content.readShort();
		this.objectList = new ObjectItem[nb];
		for(int i = 0; i < nb; ++i)
			this.objectList[i] = new ObjectItem(this.content);
	}
}