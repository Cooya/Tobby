package gamedata.context;

import gamedata.ProtocolTypeManager;

import utilities.ByteArray;

public class FightTeamInformations extends AbstractFightTeamInformations {
    public FightTeamMemberInformations[] teamMembers;

	public FightTeamInformations(ByteArray buffer) {
		super(buffer);
		int nb = buffer.readShort();
		this.teamMembers = new FightTeamMemberInformations[nb];
		for(int i = 0; i < nb; ++i)
			this.teamMembers[i] = (FightTeamMemberInformations) ProtocolTypeManager.getInstance(buffer.readShort(), buffer);
	}
}