package messages.exchanges;

import utilities.ByteArray;
import messages.Message;

public class ExchangeErrorMessage extends Message {
	public int errorType = 0;
	
	public ExchangeErrorMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.errorType = buffer.readByte();
	}
}