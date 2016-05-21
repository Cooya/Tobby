package messages.character;

import gamedata.character.PlayerStatus;
import messages.NetworkMessage;

public class PlayerStatusUpdateRequestMessage extends NetworkMessage {
	public PlayerStatus status;
	
	@Override
	public void serialize() {
		this.content.writeShort(415); // id de PlayerStatus
		this.status.serialize(this.content);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}