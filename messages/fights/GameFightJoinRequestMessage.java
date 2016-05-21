package messages.fights;

import messages.NetworkMessage;

public class GameFightJoinRequestMessage extends NetworkMessage {
	public double fighterId = 0;
	public int fightId = 0;
	
	@Override
	public void serialize() {
		this.content.writeDouble(this.fighterId);
		this.content.writeInt(this.fightId);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}