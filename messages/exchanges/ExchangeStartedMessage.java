package messages.exchanges;

import utilities.ByteArray;
import messages.Message;

public class ExchangeStartedMessage extends Message {
	public int exchangeType = 0;
	
	public ExchangeStartedMessage(Message msg) {
		super(msg);
	}
	
	protected void deserialize(ByteArray buffer) {
		this.exchangeType = buffer.readByte();
	}
}