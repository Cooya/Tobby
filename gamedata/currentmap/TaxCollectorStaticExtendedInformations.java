package gamedata.currentmap;

import utilities.ByteArray;

public class TaxCollectorStaticExtendedInformations extends TaxCollectorStaticInformations {
	public AllianceInformations allianceIdentity;

	public TaxCollectorStaticExtendedInformations(ByteArray buffer) {
		super(buffer);
		this.allianceIdentity = new AllianceInformations(buffer);
	}
}