package gamedata.currentmap;

import utilities.ByteArray;

public class FightTeamMemberCharacterInformations extends FightTeamMemberInformations {
    public String name = "";
    public int level = 0;

	public FightTeamMemberCharacterInformations(ByteArray buffer) {
		super(buffer);
		this.name = buffer.readUTF();
		this.level = buffer.readByte();
	}
}