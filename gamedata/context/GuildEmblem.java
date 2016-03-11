package gamedata.context;

import utilities.ByteArray;

public class GuildEmblem {
    public int symbolShape = 0;
    public int symbolColor = 0;
    public int backgroundShape = 0;
    public int backgroundColor = 0;

	public GuildEmblem(ByteArray buffer) {
		this.symbolShape = buffer.readVarShort();
		this.symbolColor = buffer.readInt();
		this.backgroundShape = buffer.readByte();
		this.backgroundColor = buffer.readInt();
	}
}