package messages.synchronisation;

import messages.Message;

public class BasicStatMessage extends Message {
	public int statId = 81; // ou 151...

	@Override
	public void serialize() {
		this.content.writeShort(statId);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}