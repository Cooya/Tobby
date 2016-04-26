package messages.character;

import messages.Message;

public class EmotePlayRequestMessage extends Message {
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