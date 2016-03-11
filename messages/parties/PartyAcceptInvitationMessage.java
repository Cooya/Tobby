package messages.parties;

import utilities.ByteArray;

public class PartyAcceptInvitationMessage extends AbstractPartyMessage {

	public PartyAcceptInvitationMessage() {
		super();
	}
	
	public void serialize() {
		ByteArray buffer = new ByteArray();
		super.serialize(buffer);
		super.completeInfos(buffer);
	}
}