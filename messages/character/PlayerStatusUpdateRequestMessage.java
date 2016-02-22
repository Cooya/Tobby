package messages.character;

import game.character.PlayerStatus;
import utilities.ByteArray;
import messages.Message;

public class PlayerStatusUpdateRequestMessage extends Message {
	public PlayerStatus status;
	
	public PlayerStatusUpdateRequestMessage() {
		super();
	}
	
	public void serialize(int statusId) {
		ByteArray buffer = new ByteArray();
		buffer.writeShort((short) 415);
		this.status = new PlayerStatus();
		this.status.serialize(buffer, statusId);
		completeInfos(buffer);
	}
}