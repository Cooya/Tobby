package gamedata.parties;

import java.util.Vector;

import utilities.ByteArray;
import gamedata.ProtocolTypeManager;
import gamedata.character.PlayerStatus;
import gamedata.context.EntityLook;

public class PartyGuestInformations {
    public double guestId = 0;
    public double hostId = 0;
    public String name = "";
    public EntityLook guestLook;
    public int breed = 0;
    public boolean sex = false;
    public PlayerStatus status;
    public Vector<PartyCompanionBaseInformations> companions;

    public PartyGuestInformations(ByteArray buffer) {
        this.guestId = buffer.readVarLong().toNumber();
        this.hostId = buffer.readVarLong().toNumber();
        this.name = buffer.readUTF();
        this.guestLook = new EntityLook(buffer);
        this.breed = buffer.readByte();
        this.sex = buffer.readBoolean();
        this.status = (PlayerStatus) ProtocolTypeManager.getInstance(buffer.readShort(), buffer);
        int nb = buffer.readShort();
        this.companions = new Vector<PartyCompanionBaseInformations>();
        for(int i = 0; i < nb; ++i)
        	this.companions.add(new PartyCompanionBaseInformations(buffer));
    }
}