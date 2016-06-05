package gamedata.context;

import gamedata.ProtocolTypeManager;

import utilities.ByteArray;

public class FightCommonInformations {
	public int fightId = 0;
	public int fightType = 0;
	public FightTeamInformations[] fightTeams;
	public int[] fightTeamsPositions;
	public FightOptionsInformations[] fightTeamsOptions;

	public FightCommonInformations(ByteArray buffer) {
		this.fightId = buffer.readInt();
		this.fightType = buffer.readByte();
		int nb = buffer.readShort();
		this.fightTeams = new FightTeamInformations[nb];
		for(int i = 0; i < nb; ++i)
			this.fightTeams[i] = (FightTeamInformations) ProtocolTypeManager.getInstance(buffer.readShort(), buffer);
		nb = buffer.readShort();
		this.fightTeamsPositions = new int[nb];
		for(int i = 0; i < nb; ++i)
			this.fightTeamsPositions[i] = buffer.readVarShort();
		nb = buffer.readShort();
		this.fightTeamsOptions = new FightOptionsInformations[nb];
		for(int i = 0; i < nb; ++i)
			this.fightTeamsOptions[i] = new FightOptionsInformations(buffer);
	}
}