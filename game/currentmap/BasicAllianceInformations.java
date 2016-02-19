package game.currentmap;

import utilities.ByteArray;

public class BasicAllianceInformations extends AbstractSocialGroupInfos {
    public int allianceId = 0;
    public String allianceTag = "";

	public BasicAllianceInformations(ByteArray buffer) {
		super(buffer);
		this.allianceId = buffer.readVarInt();
		this.allianceTag = buffer.readUTF();
	}
}
