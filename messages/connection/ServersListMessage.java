package messages.connection;

import gamedata.connection.GameServerInformations;

import java.util.Vector;

import messages.Message;

public class ServersListMessage extends Message {
    public Vector<GameServerInformations> servers;
    public int alreadyConnectedToServerId = 0;
    public boolean canCreateNewCharacter = false;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		this.servers = new Vector<GameServerInformations>();
		int nb = this.content.readShort();
		for(int i = 0; i < nb; ++i)
			this.servers.add(new GameServerInformations(this.content));
		this.alreadyConnectedToServerId = this.content.readVarShort();
		this.canCreateNewCharacter = this.content.readBoolean();
	}
}