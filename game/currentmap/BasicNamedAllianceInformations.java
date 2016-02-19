package game.currentmap;

import utilities.ByteArray;

public class BasicNamedAllianceInformations extends BasicAllianceInformations {
	public String allianceName = "";
	
	public BasicNamedAllianceInformations(ByteArray buffer) {
		super(buffer);
		this.allianceName = buffer.readUTF();
	}
}
