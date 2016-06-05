package messages.inventory;

import gamedata.inventory.IdolsPreset;
import gamedata.inventory.Preset;

public class InventoryContentAndPresetMessage extends InventoryContentMessage {
	public Preset[] presets;
	public IdolsPreset[] idolsPresets;
	
	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		super.deserialize();
		int nb = this.content.readShort();
		this.presets = new Preset[nb];
		for(int i = 0; i < nb; ++i)
			this.presets[i] = new Preset(this.content);
		nb = this.content.readShort();
		this.idolsPresets = new IdolsPreset[nb];
		for(int i = 0; i < nb; ++i)
			this.idolsPresets[i] = new IdolsPreset(this.content);
	}
}