package messages.context;

import messages.NetworkMessage;

public class CurrentMapMessage extends NetworkMessage {
    public int mapId;
	public String mapKey;

	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		this.mapId = this.content.readInt();
		this.mapKey = this.content.readUTF();
	}
}