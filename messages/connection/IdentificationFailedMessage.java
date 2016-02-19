package messages.connection;

import messages.Message;

public class IdentificationFailedMessage extends Message {
	public int reason = 99;
	
	public IdentificationFailedMessage(Message msg) {
		super(msg);
		this.reason = this.content[0];
	}
}