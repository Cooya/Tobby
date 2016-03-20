package messages.character;

import messages.Message;
import utilities.ByteArray;

public class BasicWhoIsRequestMessage extends Message {
	public boolean verbose = false;
	public String search = "";

	public BasicWhoIsRequestMessage() {
		super();
	}
	
	public void serialize() {
		ByteArray buffer = new ByteArray();
		buffer.writeBoolean(this.verbose);
		buffer.writeUTF(this.search);
		super.completeInfos(buffer);
	}
}