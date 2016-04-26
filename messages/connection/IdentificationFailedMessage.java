package messages.connection;

import messages.Message;

public class IdentificationFailedMessage extends Message {
	public int reason = 99;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		this.reason = this.content.readByte();
	}
}