package messages.connection;

import messages.Message;
import utilities.ByteArray;

public class CharacterCreationResultMessage extends Message {
	public int result = 1;
	
	public CharacterCreationResultMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.result = buffer.readByte();
	}
}