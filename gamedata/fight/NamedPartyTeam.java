package gamedata.fight;

import utilities.ByteArray;

public class NamedPartyTeam {
    public int teamId = 2;
    public String partyName = "";
    
    public NamedPartyTeam(ByteArray buffer) {
        this.teamId = buffer.readByte();
        this.partyName = buffer.readUTF();
    }
}