package messages.fights;

import messages.NetworkMessage;

public class GameFightTurnReadyMessage extends NetworkMessage {
	public boolean isReady = false;

	@Override
	public void serialize() {
		this.content.writeBoolean(this.isReady);
	}

	@Override
	public void deserialize() {
		// not implemented yet
	}
}