package gamedata.inventory;

import utilities.ByteArray;

public class ObjectItemQuantity extends Item {
	public int objectUID = 0;
	public int quantity = 0;

	public ObjectItemQuantity(ByteArray buffer) {
		super(buffer);
		this.objectUID = buffer.readVarInt();
		this.quantity = buffer.readVarInt();
	}
}