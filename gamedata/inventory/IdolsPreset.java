package gamedata.inventory;

import utilities.ByteArray;

public class IdolsPreset {
	public int presetId = 0;
	public int symbolId = 0;
	public int[] idolId;

	public IdolsPreset(ByteArray buffer) {
		this.presetId = buffer.readByte();
		this.symbolId = buffer.readByte();
		int nb = buffer.readShort();
		this.idolId = new int[nb];
		for(int i = 0; i < nb; ++i)
			this.idolId[i] = buffer.readVarShort();
	}
}