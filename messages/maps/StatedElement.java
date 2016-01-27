package messages.maps;

import utilities.ByteArray;

public class StatedElement {
    public int elementId = 0;
    public int elementCellId = 0;
    public int elementState = 0;

	public StatedElement(ByteArray buffer) {
        this.elementId = buffer.readInt();
        this.elementCellId = buffer.readVarShort();
        this.elementState = buffer.readVarInt();
	}
}
