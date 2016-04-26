package messages.fights;

import messages.Message;
import utilities.ByteArray;

public class GameFightTurnEndMessage extends Message {
	public double fighterId;

	public GameFightTurnEndMessage(Message msg) {
		super(msg);
		deserialize();
	}

	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.fighterId = buffer.readDouble();
	}
}