package messages.synchronisation;

import utilities.ByteArray;
import messages.Message;

public class BasicPingMessage extends Message {
	public boolean quiet = false;

	public BasicPingMessage() {
		super();
	}
	
	public void serialize(boolean quiet) {
		this.quiet = quiet;
		ByteArray buffer = new ByteArray();
		buffer.writeBoolean(this.quiet);
		completeInfos(buffer);
	}
}