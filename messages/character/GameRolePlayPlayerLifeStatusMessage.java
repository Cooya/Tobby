package messages.character;

import messages.NetworkMessage;

public class GameRolePlayPlayerLifeStatusMessage extends NetworkMessage {
	public int state = 0;
	public int phenixMapId = 0;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		this.state = this.content.readByte();
		this.phenixMapId = this.content.readInt();
	}
}