package messages.parties;

public class PartyMemberRemoveMessage extends AbstractPartyEventMessage {
	public double leavingPlayerId = 0;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		super.deserialize();
        this.leavingPlayerId = this.content.readVarLong();
	}
}