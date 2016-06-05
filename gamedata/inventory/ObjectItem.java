package gamedata.inventory;

import gamedata.ProtocolTypeManager;

import java.util.Comparator;

import utilities.ByteArray;

public class ObjectItem extends Item {
	public int position = 63;
	public int objectGID = 0;
	public ObjectEffect[] effects;
	public int objectUID = 0;
	public int quantity = 0;
	
	public int averagePrice; // rajout

	public ObjectItem(ByteArray buffer) {
		super(buffer);
		this.position = buffer.readByte();
		this.objectGID = buffer.readVarShort();
		int nb = buffer.readShort();
		this.effects = new ObjectEffect[nb];
		for(int i = 0; i < nb; ++i)
			this.effects[i] = (ObjectEffect) ProtocolTypeManager.getInstance(buffer.readShort(), buffer);
		this.objectUID = buffer.readVarInt();
		this.quantity = buffer.readVarInt();
	}
	
	public static final Comparator<ObjectItem> AVG_PRICE_DESC = (ObjectItem o1, ObjectItem o2) -> - Integer.compare(o1.averagePrice, o2.averagePrice);
}