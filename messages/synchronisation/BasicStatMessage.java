package messages.synchronisation;

import messages.NetworkMessage;

public class BasicStatMessage extends NetworkMessage {
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