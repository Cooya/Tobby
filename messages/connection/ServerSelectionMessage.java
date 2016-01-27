package messages.connection;

import utilities.ByteArray;
import messages.Message;

public class ServerSelectionMessage extends Message {
	
	public ServerSelectionMessage() {
		super();
	}
	
	public void serialize() {
		ByteArray buffer = new ByteArray();
		buffer.writeByte((byte) 11); 
		
		completeInfos(buffer);
	}
}
