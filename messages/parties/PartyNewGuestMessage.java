package messages.parties;

import utilities.ByteArray;
import gamedata.parties.PartyGuestInformations;
import messages.Message;

public class PartyNewGuestMessage extends AbstractPartyEventMessage {
	public PartyGuestInformations guest;

	public PartyNewGuestMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		super.deserialize(buffer);
		this.guest = new PartyGuestInformations(buffer);
	}
}