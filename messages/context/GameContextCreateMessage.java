package messages.context;

import utilities.ByteArray;
import messages.Message;

public class GameContextCreateMessage extends Message {
	public int context = 1;

	public GameContextCreateMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.context = buffer.readByte();
	}
}