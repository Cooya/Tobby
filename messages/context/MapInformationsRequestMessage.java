package messages.context;

import messages.Message;

public class MapInformationsRequestMessage extends Message {
	public int mapId;

	@Override
	public void serialize() {
		this.content.writeInt(this.mapId);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}