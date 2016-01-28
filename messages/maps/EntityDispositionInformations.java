package messages.maps;

import utilities.ByteArray;

public class EntityDispositionInformations {
    public int cellId = 0;
    public int direction = 1;
	
	public EntityDispositionInformations(ByteArray buffer) {
		this.cellId = buffer.readShort();
		if(this.cellId < 0 || this.cellId > 559)
			throw new Error("Invalid value read");
		this.direction = buffer.readByte();
		if(this.direction < 0 || this.direction > 7)
			throw new Error("Invalid value read");
	}
}
