package messages.fights;

import messages.NetworkMessage;

public class GameFightTurnEndMessage extends NetworkMessage {
	public double fighterId;

	@Override
	public void serialize() {
		// not implemented yet
	}

	@Override
	public void deserialize() {
		this.fighterId = this.content.readDouble();
	}
}