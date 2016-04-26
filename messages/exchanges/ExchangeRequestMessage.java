package messages.exchanges;

import messages.Message;

public class ExchangeRequestMessage extends Message {
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