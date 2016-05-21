package messages.connection;

import messages.NetworkMessage;

public class ServerSelectionMessage extends NetworkMessage {
	public int serverId = 0;
	
	@Override
	public void serialize() {
		this.content.writeByte(this.serverId); 
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}