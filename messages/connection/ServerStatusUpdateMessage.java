package messages.connection;

import game.server.GameServerInformations;
import utilities.ByteArray;
import messages.Message;

public class ServerStatusUpdateMessage extends Message {
	public GameServerInformations server;

	public ServerStatusUpdateMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.server = new GameServerInformations(buffer);
	}
}