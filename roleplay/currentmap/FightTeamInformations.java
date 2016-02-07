package roleplay.currentmap;

import java.util.Vector;

import roleplay.ProtocolTypeManager;
import utilities.ByteArray;

public class FightTeamInformations extends AbstractFightTeamInformations {
    public Vector<FightTeamMemberInformations> teamMembers;

	public FightTeamInformations(ByteArray buffer) {
		super(buffer);
		this.teamMembers = new Vector<FightTeamMemberInformations>();
		int nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			this.teamMembers.add((FightTeamMemberInformations) ProtocolTypeManager.getInstance(buffer.readShort(), buffer));
	}
}