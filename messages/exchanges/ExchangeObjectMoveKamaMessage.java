package messages.exchanges;

import messages.Message;

public class ExchangeObjectMoveKamaMessage extends Message {
	public int quantity = 0;
	
	@Override
	public void serialize() {
		this.content.writeVarInt(this.quantity);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}