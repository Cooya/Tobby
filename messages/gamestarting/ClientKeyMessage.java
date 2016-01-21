package messages.gamestarting;

import utilities.ByteArray;
import messages.Message;

public class ClientKeyMessage extends Message {
	
	public ClientKeyMessage() {
		super();
	}
	
	public void serialize(InterClientKeyManager ICKM) {
		ByteArray buffer = new ByteArray();
		buffer.writeUTF(ICKM.getFlashKey());
		
		completeInfos(buffer);
	}
}
