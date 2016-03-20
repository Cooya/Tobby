package messages.parties;

import utilities.ByteArray;
import messages.Message;

public class PartyMemberRemoveMessage extends AbstractPartyEventMessage {
	public double leavingPlayerId = 0;

	public PartyMemberRemoveMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		super.deserialize(buffer);
        this.leavingPlayerId = buffer.readVarLong();
	}
}