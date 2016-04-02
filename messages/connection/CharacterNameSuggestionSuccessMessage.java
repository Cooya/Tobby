package messages.connection;

import utilities.ByteArray;
import messages.Message;

public class CharacterNameSuggestionSuccessMessage extends Message {
	public String suggestion = "";

	public CharacterNameSuggestionSuccessMessage(Message msg) {
		super(msg);
		deserialize();
	}

	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.suggestion = buffer.readUTF();
	}
}