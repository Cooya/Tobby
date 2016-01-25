package messages.synchronisation;

import utilities.ByteArray;
import messages.Message;

public class BasicStatMessage extends Message {
	private short statId = 81;
	
	public BasicStatMessage() {
		super();
	}

	public void serialize() {
		ByteArray buffer = new ByteArray();
		buffer.writeShort(statId);
		
		completeInfos(buffer);
	}
}
