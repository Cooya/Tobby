package gamedata.currentmap;

import utilities.ByteArray;

public class GuildInformations extends BasicGuildInformations {
	public GuildEmblem guildEmblem;
	
	public GuildInformations(ByteArray buffer) {
		super(buffer);
		this.guildEmblem = new GuildEmblem(buffer);
	}
}
