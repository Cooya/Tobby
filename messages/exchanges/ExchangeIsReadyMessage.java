package messages.exchanges;

import messages.NetworkMessage;

public class ExchangeIsReadyMessage extends NetworkMessage {
    public double id = 0;
    public boolean ready = false;
	
    @Override
	public void serialize() {
		// not implemented yet
	}

    @Override
	public void deserialize() {
		this.id = this.content.readDouble();
		this.ready = this.content.readBoolean();
	}
}