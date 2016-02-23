package gamedata.server;

import utilities.ByteArray;

public class GameServerInformations {
    public int id = 0;
    public int type = -1;
    public int status = 1;
    public int completion = 0;
    public boolean isSelectable = false;
    public int charactersCount = 0;
    public int charactersSlots = 0;
    public double date = 0;

	public GameServerInformations(ByteArray buffer) {
		this.id = buffer.readVarShort();
		this.type = buffer.readByte();
		this.status = buffer.readByte();
		this.completion = buffer.readByte();
		this.isSelectable = buffer.readBoolean();
		this.charactersCount = buffer.readByte();
		this.charactersSlots = buffer.readByte();
		this.date = buffer.readDouble();
	}
}
