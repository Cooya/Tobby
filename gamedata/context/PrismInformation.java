package gamedata.context;

import utilities.ByteArray;

public class PrismInformation {
	public int typeId = 0;
	public int state = 1;
	public int nextVulnerabilityDate = 0;
	public int placementDate = 0;
	public int rewardTokenCount = 0;
	
	public PrismInformation(ByteArray buffer) {
		this.typeId = buffer.readByte();
		this.state = buffer.readByte();
		this.nextVulnerabilityDate = buffer.readInt();
		this.placementDate = buffer.readInt();
		this.rewardTokenCount = buffer.readVarInt();
	}
}