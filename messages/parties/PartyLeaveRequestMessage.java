package messages.parties;

import utilities.ByteArray;

public class PartyLeaveRequestMessage extends AbstractPartyMessage {

	public PartyLeaveRequestMessage() {
		super();
	}
	
	public void serialize() {
		ByteArray buffer = new ByteArray();
		super.serialize(buffer);
		super.completeInfos(buffer);
	}
}