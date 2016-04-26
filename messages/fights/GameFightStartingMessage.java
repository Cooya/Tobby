package messages.fights;

import messages.Message;
import utilities.ByteArray;

public class GameFightStartingMessage extends Message {
	public int fightType = 0;
	public double attackerId = 0;
	public double defenderId = 0;

	public GameFightStartingMessage(Message msg) {
		super(msg);
		deserialize();
	}

	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.fightType = buffer.readByte();
		this.attackerId = buffer.readDouble();
		this.defenderId = buffer.readDouble();
	}
}