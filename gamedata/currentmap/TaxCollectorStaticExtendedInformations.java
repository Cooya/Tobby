package gamedata.currentmap;

import utilities.ByteArray;

public class TaxCollectorStaticExtendedInformations extends TaxCollectorStaticInformations{

	 public static final int protocolId = 440;
	
	 public AllianceInformations allianceIdentity;
	 
	public TaxCollectorStaticExtendedInformations(ByteArray buffer) {
		super(buffer);
        this.allianceIdentity=new AllianceInformations(buffer);
	}

}
