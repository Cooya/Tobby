package messages.exchanges;

import messages.Message;

public class ObjectQuantityMessage extends Message {
	public int objectUID = 0;
	public int quantity = 0;

	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		this.objectUID = this.content.readVarInt();
		this.quantity = this.content.readVarInt();
	}
}