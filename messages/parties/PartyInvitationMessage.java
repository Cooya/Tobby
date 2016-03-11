package messages.parties;

import utilities.ByteArray;
import messages.Message;

public class PartyInvitationMessage extends AbstractPartyMessage {
    public int partyType = 0;
    public String partyName = "";
    public int maxParticipants = 0;
    public double fromId = 0;
    public String fromName = "";
    public double toId = 0;
	
    public PartyInvitationMessage(Message msg) {
    	super(msg);
    	deserialize();
    }
    
    private void deserialize() {
    	ByteArray buffer = new ByteArray(this.content);
    	super.deserialize(buffer);
    	this.partyType = buffer.readByte();
    	this.partyName = buffer.readUTF();
    	this.maxParticipants = buffer.readByte();
    	this.fromId = buffer.readVarLong().toNumber();
    	this.fromName = buffer.readUTF();
    	this.toId = buffer.readVarLong().toNumber();
    }
}