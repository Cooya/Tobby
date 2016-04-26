package messages.character;

import messages.Message;

public class GameRolePlayPlayerLifeStatusMessage extends Message {
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