package messages.inventory;

import gamedata.inventory.ObjectItem;

import java.util.Vector;

import messages.NetworkMessage;

public class InventoryContentMessage extends NetworkMessage {
	public Vector<ObjectItem> objects;
	public int kamas = 0;

	@Override
	public void serialize() {
		// not implemented yet
	}

	@Override
	public void deserialize() {
		int nb = this.content.readShort();
		this.objects = new Vector<ObjectItem>(nb);
		for(int i = 0; i < nb; ++i)
			this.objects.add(new ObjectItem(this.content));
		this.kamas = this.content.readVarInt();
	}
}