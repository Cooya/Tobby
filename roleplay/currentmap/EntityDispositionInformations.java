package roleplay.currentmap;

import utilities.ByteArray;

public class EntityDispositionInformations {
    public int cellId = 0;
    public int direction = 1;
	
	public EntityDispositionInformations(ByteArray buffer) {
		this.cellId = buffer.readShort();
		this.direction = buffer.readByte();
	}
}