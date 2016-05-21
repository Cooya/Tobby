package messages.exchanges;

import messages.NetworkMessage;

public class StorageKamasUpdateMessage extends NetworkMessage {
	public int kamasTotal = 0;

	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		this.kamasTotal = this.content.readInt();
	}
}