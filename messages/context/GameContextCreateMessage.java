package messages.context;

import messages.NetworkMessage;

public class GameContextCreateMessage extends NetworkMessage {
	public int context = 1;

	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		this.context = this.content.readByte();
	}
}