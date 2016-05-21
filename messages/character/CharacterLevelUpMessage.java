package messages.character;

import messages.NetworkMessage;

public class CharacterLevelUpMessage extends NetworkMessage {
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