package messages.parties;

import gamedata.ProtocolTypeManager;
import gamedata.parties.PartyGuestInformations;
import gamedata.parties.PartyMemberInformations;

import java.util.Vector;

public class PartyJoinMessage extends AbstractPartyMessage {
    public int partyType = 0;
    public double partyLeaderId = 0;
    public int maxParticipants = 0;
    public Vector<PartyMemberInformations> members;
    public Vector<PartyGuestInformations> guests;
    public boolean restricted = false;
    public String partyName = "";
    
    @Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		super.deserialize();
		this.members = new Vector<PartyMemberInformations>();
		this.guests = new Vector<PartyGuestInformations>();
		this.partyType = this.content.readByte();
        this.partyLeaderId = this.content.readVarLong();
        this.maxParticipants = this.content.readByte();
        int nb = this.content.readShort();
        for(int i = 0; i < nb; ++i)
        	this.members.add((PartyMemberInformations) ProtocolTypeManager.getInstance(this.content.readShort(), this.content));
        nb = this.content.readShort();
        for(int i = 0; i < nb; ++i)
        	this.guests.add((PartyGuestInformations) ProtocolTypeManager.getInstance(this.content.readShort(), this.content));
        this.restricted = this.content.readBoolean();
        this.partyName = this.content.readUTF();
	}
}