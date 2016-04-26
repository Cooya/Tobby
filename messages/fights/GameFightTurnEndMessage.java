package messages.fights;

import messages.Message;

public class GameFightTurnEndMessage extends Message {
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