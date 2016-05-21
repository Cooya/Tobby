package messages.character;

import messages.NetworkMessage;

public class EmotePlayRequestMessage extends NetworkMessage {
	public int emoteId = 0;

	@Override
	public void serialize() {
		this.content.writeByte(this.emoteId);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}