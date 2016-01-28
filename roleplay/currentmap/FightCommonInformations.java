package roleplay.currentmap;

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
        int protocolId;
        int nb = buffer.readShort();
        for(int i = 0; i < nb; ++i) {
        	protocolId = buffer.readShort();
        	if(protocolId == 33)
        		this.fightTeams.add(new FightTeamInformations(buffer));
        	else
        		throw new Error("Invalid or unhandled protocol id : " + protocolId + ".");
        }
        nb = buffer.readShort();
        for(int i = 0; i < nb; ++i)
        	this.fightTeamsPositions.add(buffer.readVarShort());
        nb = buffer.readShort();
        for(int i = 0; i < nb; ++i)
        	this.fightTeamsOptions.add(new FightOptionsInformations(buffer));
	}
}