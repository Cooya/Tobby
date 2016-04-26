package messages.parties;

public class PartyInvitationMessage extends AbstractPartyMessage {
    public int partyType = 0;
    public String partyName = "";
    public int maxParticipants = 0;
    public double fromId = 0;
    public String fromName = "";
    public double toId = 0;
    
    @Override
	public void serialize() {
		// not implemented yet
	}
    
    @Override
    public void deserialize() {
    	super.deserialize();
    	this.partyType = this.content.readByte();
    	this.partyName = this.content.readUTF();
    	this.maxParticipants = this.content.readByte();
    	this.fromId = this.content.readVarLong();
    	this.fromName = this.content.readUTF();
    	this.toId = this.content.readVarLong();
    }
}