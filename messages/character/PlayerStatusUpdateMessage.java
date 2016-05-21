package messages.character;

import gamedata.ProtocolTypeManager;
import gamedata.character.PlayerStatus;
import messages.NetworkMessage;

public class PlayerStatusUpdateMessage extends NetworkMessage {
    public int accountId = 0;
    public double playerId = 0;
    public PlayerStatus status;
    
    @Override
	public void serialize() {
		// not implemented yet
	}
	
    @Override
	public void deserialize() {
		this.accountId = this.content.readInt();
		this.playerId = this.content.readVarLong();
		this.status = (PlayerStatus) ProtocolTypeManager.getInstance(this.content.readShort(), this.content);
	}
}