package messages.connection;

import messages.NetworkMessage;

public class IdentificationFailedMessage extends NetworkMessage {
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