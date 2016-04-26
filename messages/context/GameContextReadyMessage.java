package messages.context;

import messages.Message;

public class GameContextReadyMessage extends Message {
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