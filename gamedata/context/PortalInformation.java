package gamedata.context;

import utilities.ByteArray;

public class PortalInformation {
	public int portalId = 0;
	public int areaId = 0;
	
	public PortalInformation(ByteArray buffer) {
		this.portalId = buffer.readInt();
		this.areaId = buffer.readShort();
	}
}