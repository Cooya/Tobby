package messages.parties;

import messages.Message;
import utilities.ByteArray;

public class PartyLeaveMessage extends AbstractPartyMessage {

	public PartyLeaveMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		super.deserialize(buffer);
	}
}