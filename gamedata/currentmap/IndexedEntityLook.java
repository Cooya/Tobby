package gamedata.currentmap;

import utilities.ByteArray;

public class IndexedEntityLook {
	public EntityLook look;
	public int index = 0;
	
	public IndexedEntityLook(ByteArray buffer) {
		this.look = new EntityLook(buffer);
		this.index = buffer.readByte();
	}
}