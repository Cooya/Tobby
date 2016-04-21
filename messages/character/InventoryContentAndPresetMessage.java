package messages.character;

import gamedata.inventory.IdolsPreset;
import gamedata.inventory.ObjectItem;
import gamedata.inventory.Preset;

import java.util.Vector;

import messages.Message;
import utilities.ByteArray;

public class InventoryContentAndPresetMessage extends Message {
	public Vector<ObjectItem> inventory;
	public int kamas;
	public Vector<Preset> presets;
	public Vector<IdolsPreset> idolsPresets;

	public InventoryContentAndPresetMessage(Message msg) {
		super(msg);
		this.inventory = new Vector<ObjectItem>();
		this.presets = new Vector<Preset>();
		this.idolsPresets = new Vector<IdolsPreset>();
		deserialize();
	}

	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		int nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			this.inventory.add(new ObjectItem(buffer));
		this.kamas = buffer.readVarInt();
		nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			this.presets.add(new Preset(buffer));
		nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			this.idolsPresets.add(new IdolsPreset(buffer));
	}
}