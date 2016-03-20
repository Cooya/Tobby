package messages.exchanges;

import messages.Message;
import utilities.ByteArray;

public class ExchangeRequestedTradeMessage extends ExchangeRequestedMessage {
	public double source = 0;
	public double target = 0;
	
	public ExchangeRequestedTradeMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		super.deserialize(buffer);
		this.source = buffer.readVarLong();
		this.target = buffer.readVarLong();
	}
}