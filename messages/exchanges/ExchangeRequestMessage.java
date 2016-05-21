package messages.exchanges;

import messages.NetworkMessage;

public class ExchangeRequestMessage extends NetworkMessage {
	public int exchangeType = 0;
	
	@Override
	public void serialize() {
		this.content.writeByte(this.exchangeType);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}