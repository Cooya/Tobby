package messages.gamestarting;

import utilities.ByteArray;
import messages.Message;

public class PrismsListRegisterMessage extends Message {
	private int listen;
	
	public PrismsListRegisterMessage() {
		super();
	}

	public void serialize() {
		ByteArray buffer = new ByteArray();
		buffer.writeByte((byte) listen); 
		
		completeInfos(buffer);
	}
}
