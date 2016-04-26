package messages.fights;

import messages.Message;
import utilities.ByteArray;

public class GameFightTurnReadyRequestMessage extends Message {
	public double id = 0;

	public GameFightTurnReadyRequestMessage(Message msg) {
		super(msg);
		deserialize();
	}

	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.id = buffer.readDouble();
	}
}