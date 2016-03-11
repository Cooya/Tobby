package gamedata.parties;

import java.util.Vector;

import utilities.ByteArray;
import gamedata.ProtocolTypeManager;
import gamedata.character.CharacterBaseInformations;
import gamedata.character.PlayerStatus;

public class PartyMemberInformations extends CharacterBaseInformations {
    public int lifePoints = 0;
    public int maxLifePoints = 0;
    public int prospecting = 0;
    public int regenRate = 0;
    public int initiative = 0;
    public int alignmentSide = 0;
    public int worldX = 0;
    public int worldY = 0;
    public int mapId = 0;
    public int subAreaId = 0;
    public PlayerStatus status;
    public Vector<PartyCompanionMemberInformations> companions;
    
    public PartyMemberInformations(ByteArray buffer) {
    	super(buffer);
        this.lifePoints = buffer.readVarInt();
        this.maxLifePoints = buffer.readVarInt();
        this.prospecting = buffer.readVarShort();
        this.regenRate = buffer.readByte();
        this.initiative = buffer.readVarShort();
        this.alignmentSide = buffer.readByte();
        this.worldX = buffer.readShort();
        this.worldY = buffer.readShort();
        this.mapId = buffer.readInt();
        this.subAreaId = buffer.readVarShort();
        this.status = (PlayerStatus) ProtocolTypeManager.getInstance(buffer.readShort(), buffer);
        int nb = buffer.readShort();
        this.companions = new Vector<PartyCompanionMemberInformations>();
        for(int i = 0; i < nb; ++i)
        	this.companions.add(new PartyCompanionMemberInformations(buffer));
    }
}