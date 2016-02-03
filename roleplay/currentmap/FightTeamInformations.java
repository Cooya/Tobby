package roleplay.currentmap;

import java.util.Vector;

import utilities.ByteArray;

public class FightTeamInformations extends AbstractFightTeamInformations {
    public Vector<FightTeamMemberInformations> teamMembers;

	public FightTeamInformations(ByteArray buffer) {
		super(buffer);
		this.teamMembers = new Vector<FightTeamMemberInformations>();
		int protocolId;
		int nb = buffer.readShort();
		for(int i = 0; i < nb; ++i) {
			protocolId = buffer.readShort();
			if(protocolId == 6)
				this.teamMembers.add(new FightTeamMemberMonsterInformations(buffer));
			else if(protocolId == 13)
				this.teamMembers.add(new FightTeamMemberCharacterInformations(buffer));
			else if(protocolId == 44)
				this.teamMembers.add(new FightTeamMemberInformations(buffer));
			else
				throw new Error("Invalid or unhandled protocol id : " + protocolId + ".");
		}
	}
}