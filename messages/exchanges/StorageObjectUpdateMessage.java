package messages.exchanges;

import gamedata.inventory.ObjectItem;
import messages.NetworkMessage;

public class StorageObjectUpdateMessage extends NetworkMessage {
	public ObjectItem object;

	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		this.object = new ObjectItem(this.content);
	}
}