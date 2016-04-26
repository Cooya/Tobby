package messages.connection;

import messages.Message;

public class CharacterCreationResultMessage extends Message {
	public int result = 1;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		this.result = this.content.readByte();
	}
}