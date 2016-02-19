package game.currentmap;

import utilities.ByteArray;

public class AllianceInformations extends BasicNamedAllianceInformations {
	public GuildEmblem allianceEmblem;
	
	public AllianceInformations(ByteArray buffer) {
		super(buffer);
		this.allianceEmblem = new GuildEmblem(buffer);
	}
}
