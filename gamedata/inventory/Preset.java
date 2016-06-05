package gamedata.inventory;

import utilities.ByteArray;

public class Preset {
	public int presetId = 0;
	public int symbolId = 0;
	public boolean mount = false;
	public PresetItem[] objects;

	public Preset(ByteArray buffer) {
		this.presetId = buffer.readByte();
		this.symbolId = buffer.readByte();
		this.mount = buffer.readBoolean();
		int nb = buffer.readShort();
		this.objects = new PresetItem[nb];
		for(int i = 0; i < nb; ++i)
			this.objects[i] = new PresetItem(buffer);
	}
}