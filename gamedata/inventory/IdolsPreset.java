package gamedata.inventory;

import java.util.Vector;

import utilities.ByteArray;

public class IdolsPreset {
	public int presetId = 0;
	public int symbolId = 0;
	public Vector<Integer> idolId;

	public IdolsPreset(ByteArray buffer) {
		this.idolId = new Vector<Integer>();
		this.presetId = buffer.readByte();
		this.symbolId = buffer.readByte();
		int nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			this.idolId.add(buffer.readVarShort());
	}
}