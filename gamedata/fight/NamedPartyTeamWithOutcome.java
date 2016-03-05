package gamedata.fight;

import utilities.ByteArray;

public class NamedPartyTeamWithOutcome {
    public NamedPartyTeam team;
    public int outcome = 0;
    
    public NamedPartyTeamWithOutcome(ByteArray buffer) {
    	this.team = new NamedPartyTeam(buffer);
        this.outcome = buffer.readVarShort();
    }
}