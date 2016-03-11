package messages.parties;

import gamedata.ProtocolTypeManager;
import gamedata.parties.PartyGuestInformations;
import gamedata.parties.PartyMemberInformations;

import java.util.Vector;

import messages.Message;
import utilities.ByteArray;

public class PartyJoinMessage extends AbstractPartyMessage {
    public int partyType = 0;
    public double partyLeaderId = 0;
    public int maxParticipants = 0;
    public Vector<PartyMemberInformations> members;
    public Vector<PartyGuestInformations> guests;
    public boolean restricted = false;
    public String partyName = "";

	public PartyJoinMessage(Message msg) {
		super(msg);
		this.members = new Vector<PartyMemberInformations>();
		this.guests = new Vector<PartyGuestInformations>();
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		super.deserialize(buffer);
		this.partyType = buffer.readByte();
        this.partyLeaderId = buffer.readVarLong().toNumber();
        this.maxParticipants = buffer.readByte();
        int nb = buffer.readShort();
        for(int i = 0; i < nb; ++i)
        	this.members.add((PartyMemberInformations) ProtocolTypeManager.getInstance(buffer.readShort(), buffer));
        nb = buffer.readShort();
        for(int i = 0; i < nb; ++i)
        	this.guests.add((PartyGuestInformations) ProtocolTypeManager.getInstance(buffer.readShort(), buffer));
        this.restricted = buffer.readBoolean();
        this.partyName = buffer.readUTF();
	}
}