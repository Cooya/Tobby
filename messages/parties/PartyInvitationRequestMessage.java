package messages.parties;

import messages.Message;

public class PartyInvitationRequestMessage extends Message {
	public String name = "";
	
	@Override
	public void serialize() {
		this.content.writeUTF(this.name);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}