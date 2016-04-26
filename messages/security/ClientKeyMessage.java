package messages.security;

import messages.Message;

public class ClientKeyMessage extends Message {
	private static final String flashKey = "lfbtPgniZWNU4QZXE6#01";
	
	@Override
	public void serialize() {
		this.content.writeUTF(flashKey);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}