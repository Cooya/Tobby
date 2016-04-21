package gamedata.inventory;

import utilities.ByteArray;

public class PresetItem {
	public int position = 63;
	public int objGid = 0;
	public int objUid = 0;

	public PresetItem(ByteArray buffer) {
		this.position = buffer.readByte();
		this.objGid = buffer.readVarShort();
		this.objUid = buffer.readVarInt();
	}
}