package messages.character;

import gamedata.inventory.SpellItem;
import messages.NetworkMessage;

public class SpellListMessage extends NetworkMessage {
	public boolean spellPrevisualization = false;
	public SpellItem[] spells;
	
	@Override
	public void serialize() {
		// not implemented yet
	}

	@Override
	public void deserialize() {
		this.spellPrevisualization = this.content.readBoolean();
		int nb = this.content.readShort();
		this.spells = new SpellItem[nb];
		for(int i = 0; i < nb; ++i)
			this.spells[i] = new SpellItem(this.content);
	}
}