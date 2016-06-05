package messages.inventory;

import gamedata.inventory.ObjectItem;

import messages.NetworkMessage;

public class InventoryContentMessage extends NetworkMessage {
	public ObjectItem[] objects;
	public int kamas = 0;

	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		int nb = this.content.readShort();
		this.objects = new ObjectItem[nb];
		for(int i = 0; i < nb; ++i)
			this.objects[i] = new ObjectItem(this.content);
		this.kamas = this.content.readVarInt();
	}
}