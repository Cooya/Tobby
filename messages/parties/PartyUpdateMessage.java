package messages.parties;

import gamedata.ProtocolTypeManager;
import gamedata.parties.PartyMemberInformations;

public class PartyUpdateMessage extends AbstractPartyEventMessage {
	public PartyMemberInformations memberInformations;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		super.deserialize();
		this.memberInformations = (PartyMemberInformations) ProtocolTypeManager.getInstance(this.content.readShort(), this.content);
	}
}