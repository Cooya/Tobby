package messages.parties;

import utilities.ByteArray;
import messages.Message;

public class PartyInvitationRequestMessage extends Message {
	public String name = "";

	public PartyInvitationRequestMessage() {
		super();
	}
	
	public void serialize() {
		ByteArray buffer = new ByteArray();
		buffer.writeUTF(this.name);
		super.completeInfos(buffer);
	}
}