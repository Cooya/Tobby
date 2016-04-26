package messages.exchanges;

import messages.Message;

public class ExchangeRequestedMessage extends Message {
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