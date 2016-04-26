package messages.character;

import messages.Message;
import utilities.ByteArray;

public class CharacterLevelUpMessage extends Message {
	public int newLevel = 0;

	public CharacterLevelUpMessage(Message msg) {
		super(msg);
		deserialize();
	}

	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.newLevel = buffer.readByte();
	}
}