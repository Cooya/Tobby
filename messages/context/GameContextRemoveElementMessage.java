package messages.context;

import messages.Message;

public class GameContextRemoveElementMessage extends Message {
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