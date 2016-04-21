package gamedata.inventory;

import java.util.Vector;

import utilities.ByteArray;

public class Preset {
	public int presetId = 0;
	public int symbolId = 0;
	public boolean mount = false;
	public Vector<PresetItem> objects;

	public Preset(ByteArray buffer) {
		this.objects = new Vector<PresetItem>();
		this.presetId = buffer.readByte();
		this.symbolId = buffer.readByte();
		this.mount = buffer.readBoolean();
		int nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			this.objects.add(new PresetItem(buffer));
	}
}