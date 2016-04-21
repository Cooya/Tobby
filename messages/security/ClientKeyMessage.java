package messages.security;

import utilities.ByteArray;
import main.Emulation;
import messages.Message;

public class ClientKeyMessage extends Message {
	private static final String flashKey = "lfbtPgniZWNU4QZXE6#01";
	
	public ClientKeyMessage() {
		super();
	}
	
	public void serialize(int instanceId) {
		ByteArray buffer = new ByteArray();
		buffer.writeUTF(flashKey);
		
		completeInfos(Emulation.hashMessage(buffer, instanceId));
	}
}