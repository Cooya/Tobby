package messages.exchanges;

import messages.NetworkMessage;

public class ExchangeObjectMoveKamaMessage extends NetworkMessage {
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