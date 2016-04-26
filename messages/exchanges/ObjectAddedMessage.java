package messages.exchanges;

import messages.Message;
import gamedata.inventory.ObjectItem;

public class ObjectAddedMessage extends Message {
	public ObjectItem object;

	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		this.object = new ObjectItem(this.content);
	}
}