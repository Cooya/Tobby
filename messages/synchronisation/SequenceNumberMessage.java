package messages.synchronisation;

import messages.NetworkMessage;

public class SequenceNumberMessage extends NetworkMessage {
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