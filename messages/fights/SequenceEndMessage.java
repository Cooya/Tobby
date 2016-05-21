package messages.fights;

import messages.NetworkMessage;

public class SequenceEndMessage extends NetworkMessage {
	public int actionId = 0;
	public double authorId = 0;
	public int sequenceType = 0;

	@Override
	public void serialize() {
		// not implemented yet
	}

	@Override
	public void deserialize() {
		this.actionId = this.content.readVarShort();
		this.authorId = this.content.readDouble();
		this.sequenceType = this.content.readByte();
	}
}