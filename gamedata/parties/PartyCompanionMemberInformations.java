package gamedata.parties;

import utilities.ByteArray;

public class PartyCompanionMemberInformations extends PartyCompanionBaseInformations {
	public int initiative = 0;
	public int lifePoints = 0;
	public int maxLifePoints = 0;
	public int prospecting = 0;
	public int regenRate = 0;
	
	public PartyCompanionMemberInformations(ByteArray buffer) {
		super(buffer);
		this.initiative = buffer.readVarShort();
        this.lifePoints = buffer.readVarInt();
        this.maxLifePoints = buffer.readVarInt();
        this.prospecting = buffer.readVarShort();
        this.regenRate = buffer.readByte();
	}
}