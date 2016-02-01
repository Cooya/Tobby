package roleplay.currentmap;

import utilities.ByteArray;

public class HumanOptionAlliance extends HumanOption {
    public AllianceInformations allianceInformations;
    public int aggressable = 0;

	public HumanOptionAlliance(ByteArray buffer) {
		super(buffer);
		this.allianceInformations = new AllianceInformations(buffer);
		this.aggressable = buffer.readByte();
	}
}
