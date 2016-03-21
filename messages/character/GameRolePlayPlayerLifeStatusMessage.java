package messages.character;

import utilities.ByteArray;
import messages.Message;

public class GameRolePlayPlayerLifeStatusMessage extends Message {
	public int state = 0;
	public int phenixMapId = 0;
	
	public GameRolePlayPlayerLifeStatusMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.state = buffer.readByte();
		this.phenixMapId = buffer.readInt();
	}
}