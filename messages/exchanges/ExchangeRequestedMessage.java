package messages.exchanges;

import messages.NetworkMessage;

public class ExchangeRequestedMessage extends NetworkMessage {
	public int exchangeType = 0;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		this.exchangeType = this.content.readByte();
	}
}