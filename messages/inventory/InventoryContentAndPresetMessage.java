package messages.inventory;

import gamedata.inventory.IdolsPreset;
import gamedata.inventory.Preset;

import java.util.Vector;

public class InventoryContentAndPresetMessage extends InventoryContentMessage {
	public Vector<Preset> presets;
	public Vector<IdolsPreset> idolsPresets;
	
	@Override
	public void serialize() {
		// not implemented yet
	}

	@Override
	public void deserialize() {
		super.deserialize();
		int nb = this.content.readShort();
		this.presets = new Vector<Preset>(nb);
		for(int i = 0; i < nb; ++i)
			this.presets.add(new Preset(this.content));
		nb = this.content.readShort();
		this.idolsPresets = new Vector<IdolsPreset>(nb);
		for(int i = 0; i < nb; ++i)
			this.idolsPresets.add(new IdolsPreset(this.content));
	}
}