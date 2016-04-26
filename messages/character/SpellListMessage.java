package messages.character;

import java.util.Vector;

import gamedata.inventory.SpellItem;
import messages.Message;
import utilities.ByteArray;

public class SpellListMessage extends Message {
	public boolean spellPrevisualization = false;
	public Vector<SpellItem> spells;
	
	public SpellListMessage(Message msg) {
		super(msg);
		spells = new Vector<SpellItem>();
		deserialize();
	}

	private void deserialize() {
		ByteArray buffer = new ByteArray(content);
		this.spellPrevisualization = buffer.readBoolean();
		int nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			this.spells.add(new SpellItem(buffer));
	}
}