package messages.character;

import messages.Message;
import utilities.ByteArray;

public class SpellModifyRequestMessage extends Message {
	public int spellId = 0;
	public int spellLevel = 0;
	
	public SpellModifyRequestMessage() {
		super();
	}
	
	public void serialize() {
		ByteArray buffer = new ByteArray();
		buffer.writeVarShort(this.spellId);
		buffer.writeByte(this.spellLevel);
		super.completeInfos(buffer);
	}
}