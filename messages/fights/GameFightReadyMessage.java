package messages.fights;

import messages.NetworkMessage;

public class GameFightReadyMessage extends NetworkMessage {
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