package messages.connection;

import messages.NetworkMessage;

public class CharacterCreationResultMessage extends NetworkMessage {
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