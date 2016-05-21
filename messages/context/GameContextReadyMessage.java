package messages.context;

import messages.NetworkMessage;

public class GameContextReadyMessage extends NetworkMessage {
	public int mapId = 0;

	@Override
	public void serialize() {
		this.content.writeInt(this.mapId);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}