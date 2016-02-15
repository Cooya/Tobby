package roleplay.inventory;

import java.util.Vector;

import roleplay.ProtocolTypeManager;
import utilities.ByteArray;

public class ObjectItem extends Item {
	public int position = 63;
	public int objectGID = 0;
	public Vector<ObjectEffect> effects;
	public int objectUID = 0;
	public int quantity = 0;
	
	public ObjectItem(ByteArray buffer) {
		this.effects = new Vector<ObjectEffect>();
		this.position = buffer.readByte();
		this.objectGID = buffer.readVarShort();
		int nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			this.effects.add((ObjectEffect) ProtocolTypeManager.getInstance(buffer.readShort(), buffer));
		this.objectUID = buffer.readVarInt();
		this.quantity = buffer.readVarInt();
	}
}