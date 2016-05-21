package messages.parties;

import messages.NetworkMessage;

public class AbstractPartyMessage extends NetworkMessage {
	public int partyId = 0;
	
	@Override
	public void serialize() {
		this.content.writeVarInt(this.partyId);
	}
	
	@Override
	public void deserialize() {
		this.partyId = this.content.readVarInt();
	}
}