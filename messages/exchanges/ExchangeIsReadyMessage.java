package messages.exchanges;

import messages.Message;
import utilities.ByteArray;

public class ExchangeIsReadyMessage extends Message {
    public double id = 0;
    public boolean ready = false;
	
	public ExchangeIsReadyMessage(Message msg) {
		super(msg);
		deserialize();
	}

	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.id = buffer.readDouble();
		this.ready = buffer.readBoolean();
	}
}