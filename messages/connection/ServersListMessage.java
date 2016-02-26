package messages.connection;

import gamedata.server.GameServerInformations;

import java.util.Vector;

import main.FatalError;
import messages.Message;
import utilities.ByteArray;

public class ServersListMessage extends Message {
    public Vector<GameServerInformations> servers;
    public int alreadyConnectedToServerId = 0;
    public boolean canCreateNewCharacter = false;

	public ServersListMessage(Message msg) {
		super(msg);
		this.servers = new Vector<GameServerInformations>();
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		int nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			this.servers.add(new GameServerInformations(buffer));
		this.alreadyConnectedToServerId = buffer.readVarShort();
		this.canCreateNewCharacter = buffer.readBoolean();
	}
	
	public boolean isSelectable(int serverId) {
		for(GameServerInformations server : servers)
			if(server.id == serverId)
				if(!server.isSelectable)
					return false;
				else
					return true;
		throw new FatalError("Invalid server id.");
	}
}
