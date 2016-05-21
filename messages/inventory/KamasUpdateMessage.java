package messages.inventory;

import messages.NetworkMessage;

public class KamasUpdateMessage extends NetworkMessage {
	public int kamasTotal = 0;

	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		this.kamasTotal = this.content.readVarInt();
	}
}