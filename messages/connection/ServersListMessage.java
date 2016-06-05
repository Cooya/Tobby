package messages.connection;

import gamedata.connection.GameServerInformations;

import messages.NetworkMessage;

public class ServersListMessage extends NetworkMessage {
    public GameServerInformations[] servers;
    public int alreadyConnectedToServerId = 0;
    public boolean canCreateNewCharacter = false;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		int nb = this.content.readShort();
		this.servers = new GameServerInformations[nb];
		for(int i = 0; i < nb; ++i)
			this.servers[i] = new GameServerInformations(this.content);
		this.alreadyConnectedToServerId = this.content.readVarShort();
		this.canCreateNewCharacter = this.content.readBoolean();
	}
}