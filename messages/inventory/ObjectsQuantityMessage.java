package messages.inventory;

import gamedata.inventory.ObjectItemQuantity;

import java.util.Vector;

import messages.NetworkMessage;

public class ObjectsQuantityMessage extends NetworkMessage {
	public Vector<ObjectItemQuantity> objectsUIDAndQty;

	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		int nb = this.content.readShort();
		this.objectsUIDAndQty = new Vector<ObjectItemQuantity>(nb);
		for(int i = 0; i < nb; ++i)
			this.objectsUIDAndQty.add(new ObjectItemQuantity(this.content));
	}
}