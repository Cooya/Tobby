package messages.inventory;

import gamedata.inventory.ObjectItemQuantity;

import messages.NetworkMessage;

public class ObjectsQuantityMessage extends NetworkMessage {
	public ObjectItemQuantity[] objectsUIDAndQty;

	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		int nb = this.content.readShort();
		this.objectsUIDAndQty = new ObjectItemQuantity[nb];
		for(int i = 0; i < nb; ++i)
			this.objectsUIDAndQty[i] = new ObjectItemQuantity(this.content);
	}
}