package messages.parties;

import gamedata.parties.PartyGuestInformations;

public class PartyNewGuestMessage extends AbstractPartyEventMessage {
	public PartyGuestInformations guest;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		super.deserialize();
		this.guest = new PartyGuestInformations(this.content);
	}
}