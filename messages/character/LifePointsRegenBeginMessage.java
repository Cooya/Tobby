package messages.character;

import utilities.ByteArray;
import messages.Message;

public class LifePointsRegenBeginMessage extends Message {
	public int regenRate = 0;

	public LifePointsRegenBeginMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.regenRate = buffer.readByte();
	}
}