package messages.character;

import gamedata.ProtocolTypeManager;
import gamedata.character.PlayerStatus;
import utilities.ByteArray;
import messages.Message;

public class PlayerStatusUpdateMessage extends Message {
    public int accountId = 0;
    public double playerId = 0;
    public PlayerStatus status;

	public PlayerStatusUpdateMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.accountId = buffer.readInt();
		this.playerId = buffer.readVarLong();
		this.status = (PlayerStatus) ProtocolTypeManager.getInstance(buffer.readShort(), buffer);
	}
}