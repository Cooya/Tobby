package messages.parties;

import messages.NetworkMessage;

public class PartyInvitationRequestMessage extends NetworkMessage {
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