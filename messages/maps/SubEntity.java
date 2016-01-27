package messages.maps;

import utilities.ByteArray;

public class SubEntity {
    public int bindingPointCategory = 0;
    public int bindingPointIndex = 0;
    public EntityLook subEntityLook;

	public SubEntity(ByteArray buffer) {
		this.bindingPointCategory = buffer.readByte();
		this.bindingPointCategory = buffer.readByte();
		this.subEntityLook = new EntityLook(buffer);
	}
}
