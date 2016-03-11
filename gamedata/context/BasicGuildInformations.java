package gamedata.context;

import utilities.ByteArray;

public class BasicGuildInformations extends AbstractSocialGroupInfos {
    public int guildId = 0;
    public String guildName = "";
    public int guildLevel = 0;

    public BasicGuildInformations(ByteArray buffer) {
    	super(buffer);
    	this.guildId = buffer.readVarInt();
    	this.guildName = buffer.readUTF();
    	this.guildLevel = buffer.readByte();
    }
}
