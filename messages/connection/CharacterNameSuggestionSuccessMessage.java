package messages.connection;

import messages.NetworkMessage;

public class CharacterNameSuggestionSuccessMessage extends NetworkMessage {
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