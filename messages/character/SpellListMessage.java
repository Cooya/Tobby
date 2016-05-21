package messages.character;

import java.util.Vector;

import gamedata.inventory.SpellItem;
import messages.NetworkMessage;

public class SpellListMessage extends NetworkMessage {
	public boolean spellPrevisualization = false;
	public Vector<SpellItem> spells;
	
	@Override
	public void serialize() {
		// not implemented yet
	}

	@Override
	public void deserialize() {
		this.spells = new Vector<SpellItem>();
		this.spellPrevisualization = this.content.readBoolean();
		int nb = this.content.readShort();
		for(int i = 0; i < nb; ++i)
			this.spells.add(new SpellItem(this.content));
	}
}