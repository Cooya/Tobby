package messages.exchange;

import utilities.ByteArray;
import messages.Message;

public class ExchangeRequestedMessage extends Message {
	public int exchangeType = 0;
	
	public ExchangeRequestedMessage(Message msg) {
		super(msg);
	}
	
	protected void deserialize(ByteArray buffer) {
		this.exchangeType = buffer.readByte();
	}
}