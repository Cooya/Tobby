package messages.character;

import messages.Message;

public class SpellModifyRequestMessage extends Message {
	public int spellId = 0;
	public int spellLevel = 0;
	
	@Override
	public void serialize() {
		this.content.writeVarShort(this.spellId);
		this.content.writeByte(this.spellLevel);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}