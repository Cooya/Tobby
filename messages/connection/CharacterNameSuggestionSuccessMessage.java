package messages.connection;

import messages.Message;

public class CharacterNameSuggestionSuccessMessage extends Message {
	public String suggestion = "";
	
	@Override
	public void serialize() {
		// not implemented yet
	}

	@Override
	public void deserialize() {
		this.suggestion = this.content.readUTF();
	}
}