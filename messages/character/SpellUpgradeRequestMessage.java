package messages.character;

import messages.Message;
import utilities.ByteArray;

public class SpellUpgradeRequestMessage extends Message {
	public int spellId = 0;
	public int spellLevel = 0;

	public SpellUpgradeRequestMessage() {
		super();
	}

	public void serialize(int spellId, int spellLevel) {
		ByteArray buffer = new ByteArray();
		buffer.writeVarShort(spellId);
		buffer.writeByte(spellLevel);
		this.completeInfos(buffer);
	}
}