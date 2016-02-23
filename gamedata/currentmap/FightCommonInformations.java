package gamedata.currentmap;

import gamedata.ProtocolTypeManager;

import java.util.Vector;

import utilities.ByteArray;

public class FightCommonInformations {
    public int fightId = 0;
    public int fightType = 0;
    public Vector<FightTeamInformations> fightTeams;
    public Vector<Integer> fightTeamsPositions;
    public Vector<FightOptionsInformations> fightTeamsOptions;
    
	public FightCommonInformations(ByteArray buffer) {
        this.fightTeams = new Vector<FightTeamInformations>();
        this.fightTeamsPositions = new Vector<Integer>();
        this.fightTeamsOptions = new Vector<FightOptionsInformations>();
		
        this.fightId = buffer.readInt();
        this.fightType = buffer.readByte();
        int nb = buffer.readShort();
        for(int i = 0; i < nb; ++i)
        	this.fightTeams.add((FightTeamInformations) ProtocolTypeManager.getInstance(buffer.readShort(), buffer));
        nb = buffer.readShort();
        for(int i = 0; i < nb; ++i)
        	this.fightTeamsPositions.add(buffer.readVarShort());
        nb = buffer.readShort();
        for(int i = 0; i < nb; ++i)
        	this.fightTeamsOptions.add(new FightOptionsInformations(buffer));
	}
}