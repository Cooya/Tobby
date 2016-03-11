package gamedata.context;

import utilities.ByteArray;

public class HouseInformationsExtended extends HouseInformations {
	public GuildInformations guildInfo;
	
	public HouseInformationsExtended(ByteArray buffer) {
		super(buffer);
		this.guildInfo = new GuildInformations(buffer);
	}
}