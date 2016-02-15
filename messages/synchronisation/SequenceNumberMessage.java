package messages.synchronisation;

import utilities.ByteArray;
import messages.Message;

public class SequenceNumberMessage extends Message {
	
	public SequenceNumberMessage() {
		super();
	}
	
	public void serialize(int sequenceNumber) {
		ByteArray buffer = new ByteArray();
		buffer.writeShort((short) sequenceNumber);
		
		completeInfos(buffer);
	}
}
