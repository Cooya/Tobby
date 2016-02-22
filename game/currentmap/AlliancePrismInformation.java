package game.currentmap;

import utilities.ByteArray;

public class AlliancePrismInformation extends PrismInformation{
	
	public static int protocolId = 427;
    
    public AllianceInformations alliance;

	public AlliancePrismInformation(ByteArray buffer) {
		super(buffer);
		this.alliance = new AllianceInformations(buffer);
	}

}
