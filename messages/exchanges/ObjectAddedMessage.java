package messages.exchanges;

import messages.Message;
import gamedata.inventory.ObjectItem;
import utilities.ByteArray;

public class ObjectAddedMessage extends Message {
	public ObjectItem object;

	public ObjectAddedMessage(Message msg) {
		super(msg);
		deserialize();
	}

	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.object = new ObjectItem(buffer);
	}
}