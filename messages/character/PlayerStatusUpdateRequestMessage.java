package messages.character;

import gamedata.character.PlayerStatus;
import utilities.ByteArray;
import messages.Message;

public class PlayerStatusUpdateRequestMessage extends Message {
	public PlayerStatus status;
	
	public PlayerStatusUpdateRequestMessage() {
		super();
	}
	
	public void serialize(int statusId) {
		ByteArray buffer = new ByteArray();
		buffer.writeShort(415);
		buffer.writeByte(statusId); // raccourci (PlayerStatus normalement)
		completeInfos(buffer);
	}
}