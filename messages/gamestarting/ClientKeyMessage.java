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
		
		this.size = buffer.getSize();
		this.lenofsize = computeLenOfSize(this.size);
		this.content = buffer.bytes();
	}
}
