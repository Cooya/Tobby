package messages.maps;

import utilities.ByteArray;

public class AbstractFightTeamInformations {
    public int teamId = 2;
    public double leaderId = 0;
    public int teamSide = 0;
    public int teamTypeId = 0;
    public int nbWaves = 0;

	public AbstractFightTeamInformations(ByteArray buffer) {	
        this.teamId = buffer.readByte();
        this.leaderId = buffer.readDouble();
        this.teamSide = buffer.readByte();
        this.teamTypeId = buffer.readByte();
        this.nbWaves = buffer.readByte();
	}
}