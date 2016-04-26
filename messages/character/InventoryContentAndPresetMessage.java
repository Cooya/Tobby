package messages.character;

import gamedata.inventory.IdolsPreset;
import gamedata.inventory.ObjectItem;
import gamedata.inventory.Preset;

import java.util.Vector;

import messages.Message;

public class InventoryContentAndPresetMessage extends Message {
	public Vector<ObjectItem> inventory;
	public int kamas;
	public Vector<Preset> presets;
	public Vector<IdolsPreset> idolsPresets;
	
	@Override
	public void serialize() {
		// not implemented yet
	}

	@Override
	public void deserialize() {
		this.inventory = new Vector<ObjectItem>();
		this.presets = new Vector<Preset>();
		this.idolsPresets = new Vector<IdolsPreset>();
		int nb = this.content.readShort();
		for(int i = 0; i < nb; ++i)
			this.inventory.add(new ObjectItem(this.content));
		this.kamas = this.content.readVarInt();
		nb = this.content.readShort();
		for(int i = 0; i < nb; ++i)
			this.presets.add(new Preset(this.content));
		nb = this.content.readShort();
		for(int i = 0; i < nb; ++i)
			this.idolsPresets.add(new IdolsPreset(this.content));
	}
}