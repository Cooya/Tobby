package game.currentmap;

import utilities.ByteArray;

public class FightTeamMemberWithAllianceCharacterInformations extends FightTeamMemberCharacterInformations {
	public BasicAllianceInformations allianceInfos;
	
	public FightTeamMemberWithAllianceCharacterInformations(ByteArray buffer) {
		super(buffer);
		this.allianceInfos = new BasicAllianceInformations(buffer);
	}
}