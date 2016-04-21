package gamedata.inventory;

import utilities.ByteArray;

public class SpellItem extends Item {
	public int spellId = 0;
	public int spellLevel = 0;

	public SpellItem(ByteArray buffer) {
		super(buffer);
		this.spellId = buffer.readInt();
		this.spellLevel = buffer.readByte();
	}
}