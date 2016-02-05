package messages.synchronisation;

import utilities.ByteArray;
import messages.Message;

public class BasicStatMessage extends Message {
	public int statId = 81; // ou 151...
	
	public BasicStatMessage() {
		super();
	}

	public void serialize() {
		ByteArray buffer = new ByteArray();
		buffer.writeShort((short) statId);
		
		completeInfos(buffer);
	}
}
