package gamedata.context;

import utilities.ByteArray;

public class GameRolePlayMountInformations extends GameRolePlayNamedActorInformations {
	public String ownerName = "";
	public int level = 0;

	public GameRolePlayMountInformations(ByteArray buffer) {
		super(buffer);
		this.ownerName = buffer.readUTF();
		this.level = buffer.readByte();
	}
}