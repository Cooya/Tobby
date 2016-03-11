package messages.parties;

import utilities.ByteArray;
import messages.Message;

public class AbstractPartyEventMessage extends AbstractPartyMessage {

	public AbstractPartyEventMessage(Message msg) {
		super(msg);
	}
	
	protected void deserialize(ByteArray buffer) {
		super.deserialize(buffer);
	}
}