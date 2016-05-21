package messages.context;

import messages.NetworkMessage;

public class GameContextRemoveElementMessage extends NetworkMessage {
	public double id = 0;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		this.id = this.content.readDouble();
	}
}