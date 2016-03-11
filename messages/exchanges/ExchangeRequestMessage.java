package messages.exchanges;

import utilities.ByteArray;
import messages.Message;

public class ExchangeRequestMessage extends Message {
	public int exchangeType = 0;
	
	public ExchangeRequestMessage() {
		super();
	}
	
	protected void serialize(ByteArray buffer, int exchangeType) {
		this.exchangeType = exchangeType;
		buffer.writeByte(this.exchangeType);
	}
}