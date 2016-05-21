package messages.inventory;

import messages.NetworkMessage;
import gamedata.inventory.ObjectItem;

public class ObjectAddedMessage extends NetworkMessage {
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