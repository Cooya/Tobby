package messages.character;

import messages.Message;

public class CharacterLevelUpMessage extends Message {
	public int newLevel = 0;
	
	@Override
	public void serialize() {
		// not implemented yet
	}

	@Override
	public void deserialize() {
		this.newLevel = this.content.readByte();
	}
}