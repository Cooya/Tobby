package gamedata.character;

import utilities.ByteArray;

public class PlayerStatus {
	public int statusId = 1;

	public PlayerStatus(int statusId) {
		this.statusId = statusId;
	}

	public PlayerStatus(ByteArray buffer) {
		this.statusId = buffer.readByte();
	}

	public void serialize(ByteArray buffer) {
		buffer.writeByte(this.statusId);
	}
}