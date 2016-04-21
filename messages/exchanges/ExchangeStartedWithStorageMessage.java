package messages.exchanges;

import utilities.ByteArray;
import messages.Message;

public class ExchangeStartedWithStorageMessage extends ExchangeStartedMessage {
	public int storageMaxSlot = 0;
	
	public ExchangeStartedWithStorageMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		super.deserialize(buffer);
		this.storageMaxSlot = buffer.readVarInt();
	}
}