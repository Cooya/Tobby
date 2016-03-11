package gamedata.context;

import utilities.ByteArray;

public class FightTeamMemberMonsterInformations extends FightTeamMemberInformations {
    public int monsterId = 0;
    public int grade = 0;
    
    public FightTeamMemberMonsterInformations(ByteArray buffer) {
    	super(buffer);
    	this.monsterId = buffer.readInt();
    	this.grade = buffer.readByte();
    }
}