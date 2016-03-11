package gamedata.context;

import utilities.ByteArray;

public class AlliancePrismInformation extends PrismInformation {
    public AllianceInformations alliance;

	public AlliancePrismInformation(ByteArray buffer) {
		super(buffer);
		this.alliance = new AllianceInformations(buffer);
	}
}