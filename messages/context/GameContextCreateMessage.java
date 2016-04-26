package messages.context;

import messages.Message;

public class GameContextCreateMessage extends Message {
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