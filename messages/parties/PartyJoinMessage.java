package messages.parties;

import gamedata.ProtocolTypeManager;
import gamedata.parties.PartyGuestInformations;
import gamedata.parties.PartyMemberInformations;

public class PartyJoinMessage extends AbstractPartyMessage {
    public int partyType = 0;
    public double partyLeaderId = 0;
    public int maxParticipants = 0;
    public PartyMemberInformations[] members;
    public PartyGuestInformations[] guests;
    public boolean restricted = false;
    public String partyName = "";
    
    @Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		super.deserialize();
		this.partyType = this.content.readByte();
        this.partyLeaderId = this.content.readVarLong();
        this.maxParticipants = this.content.readByte();
        int nb = this.content.readShort();
        this.members = new PartyMemberInformations[nb];
        for(int i = 0; i < nb; ++i)
        	this.members[i] = (PartyMemberInformations) ProtocolTypeManager.getInstance(this.content.readShort(), this.content);
        nb = this.content.readShort();
        this.guests = new PartyGuestInformations[nb];
        for(int i = 0; i < nb; ++i)
        	this.guests[i] = (PartyGuestInformations) ProtocolTypeManager.getInstance(this.content.readShort(), this.content);
        this.restricted = this.content.readBoolean();
        this.partyName = this.content.readUTF();
	}
}