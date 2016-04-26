package messages.exchanges;

import messages.Message;

public class ExchangeErrorMessage extends Message {
	public int errorType = 0;

	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		this.errorType = this.content.readByte();
	}
}