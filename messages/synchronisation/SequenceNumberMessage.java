package messages.synchronisation;

import messages.Message;

public class SequenceNumberMessage extends Message {
	public int number = 0;
	
	@Override
	public void serialize() {
		this.content.writeShort(this.number);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}