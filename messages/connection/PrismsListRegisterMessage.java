package messages.connection;

import utilities.ByteArray;
import messages.Message;

public class PrismsListRegisterMessage extends Message {
	public int listen;
	
	public PrismsListRegisterMessage() {
		super();
	}

	public void serialize() {
		ByteArray buffer = new ByteArray();
		buffer.writeByte((byte) listen); 
		
		completeInfos(buffer);
	}
}
