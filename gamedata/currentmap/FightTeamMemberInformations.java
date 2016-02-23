package gamedata.currentmap;

import utilities.ByteArray;

public class FightTeamMemberInformations {
	public double id = 0;

	public FightTeamMemberInformations(ByteArray buffer) {
		this.id = buffer.readDouble();
	}
}