package messages.exchanges;

import messages.NetworkMessage;

public class ExchangeErrorMessage extends NetworkMessage {
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