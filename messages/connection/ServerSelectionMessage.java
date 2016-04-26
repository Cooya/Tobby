package messages.connection;

import messages.Message;

public class ServerSelectionMessage extends Message {
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