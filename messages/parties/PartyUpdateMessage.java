package messages.parties;

import gamedata.ProtocolTypeManager;
import gamedata.parties.PartyMemberInformations;
import utilities.ByteArray;
import messages.Message;

public class PartyUpdateMessage extends AbstractPartyEventMessage {
	public PartyMemberInformations memberInformations;

	public PartyUpdateMessage(Message msg) {
		super(msg);
	}
	
	protected void deserialize(ByteArray buffer) {
		super.deserialize(buffer);
		this.memberInformations = (PartyMemberInformations) ProtocolTypeManager.getInstance(buffer.readShort(), buffer);
	}
}