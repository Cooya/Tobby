package messages.parties;

import utilities.ByteArray;
import messages.Message;

public class AbstractPartyMessage extends Message {
	public int partyId = 0;
	
	public AbstractPartyMessage() {
		super();
	}
	
	public AbstractPartyMessage(Message msg) {
		super(msg);
	}
	
	protected void serialize(ByteArray buffer) {
		buffer.writeVarInt(this.partyId);
	}
	
	protected void deserialize(ByteArray buffer) {
		this.partyId = buffer.readVarInt();
	}
}