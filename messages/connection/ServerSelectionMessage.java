package messages.connection;

import utilities.ByteArray;
import messages.Message;

public class ServerSelectionMessage extends Message {
	
	public ServerSelectionMessage() {
		super();
	}
	
	public void serialize(int serverId) {
		ByteArray buffer = new ByteArray();
		buffer.writeByte((byte) serverId); 
		
		completeInfos(buffer);
	}
}
