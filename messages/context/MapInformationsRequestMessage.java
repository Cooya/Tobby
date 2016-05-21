package messages.context;

import messages.NetworkMessage;

public class MapInformationsRequestMessage extends NetworkMessage {
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