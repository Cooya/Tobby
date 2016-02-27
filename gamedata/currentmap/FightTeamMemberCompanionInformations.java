package gamedata.currentmap;

import utilities.ByteArray;

public class FightTeamMemberCompanionInformations extends FightTeamMemberInformations {
    public int companionId = 0;
    public int level = 0;
    public int masterId = 0;
	
	public FightTeamMemberCompanionInformations(ByteArray buffer) {
        super(buffer);
        this.companionId = buffer.readByte();
        this.level = buffer.readByte();
        this.masterId = buffer.readInt();
	}
}