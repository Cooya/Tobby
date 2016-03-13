package messages.parties;

import utilities.ByteArray;
import messages.Message;

public class PartyDeletedMessage extends AbstractPartyMessage {

	public PartyDeletedMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		super.deserialize(buffer);
	}
}