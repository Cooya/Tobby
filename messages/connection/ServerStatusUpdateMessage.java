package messages.connection;

import gamedata.connection.GameServerInformations;
import messages.Message;

public class ServerStatusUpdateMessage extends Message {
	public GameServerInformations server;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		this.server = new GameServerInformations(this.content);
	}
}