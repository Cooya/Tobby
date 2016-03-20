package messages.fights;

import messages.Message;
import utilities.ByteArray;

public class AbstractGameActionMessage extends Message {
	public int actionId = 0;
	public double sourceId = 0;

	public AbstractGameActionMessage(Message msg) {
		super(msg);
	}

	protected void deserialize(ByteArray buffer) {
		this.actionId = buffer.readVarShort();
		this.sourceId = buffer.readDouble();
	}
}