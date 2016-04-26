package messages.fights;

import messages.Message;
import utilities.ByteArray;

public class SequenceEndMessage extends Message {
	public int actionId = 0;
	public double authorId = 0;
	public int sequenceType = 0;

	public SequenceEndMessage(Message msg) {
		super(msg);
		deserialize();
	}

	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.actionId = buffer.readVarShort();
		this.authorId = buffer.readDouble();
		this.sequenceType = buffer.readByte();
	}
}