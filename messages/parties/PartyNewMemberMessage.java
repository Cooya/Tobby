package messages.parties;

import utilities.ByteArray;
import messages.Message;

public class PartyNewMemberMessage extends PartyUpdateMessage {

	public PartyNewMemberMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		super.deserialize(buffer);
	}
}