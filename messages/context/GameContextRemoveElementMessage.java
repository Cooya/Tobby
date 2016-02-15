package messages.context;

import messages.Message;
import utilities.ByteArray;

public class GameContextRemoveElementMessage extends Message {
	public double id = 0;
	
	public GameContextRemoveElementMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.id = buffer.readDouble();
	}
}