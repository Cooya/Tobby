package messages.exchanges;

import messages.NetworkMessage;

public class ExchangeObjectMessage extends NetworkMessage {
	public boolean remote = false;

	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		this.remote = this.content.readBoolean();
	}
}