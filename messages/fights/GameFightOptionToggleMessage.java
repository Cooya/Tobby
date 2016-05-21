package messages.fights;

import messages.NetworkMessage;

public class GameFightOptionToggleMessage extends NetworkMessage {
	public int option = 3;

	@Override
	public void serialize() {
		this.content.writeByte(this.option);
	}

	@Override
	public void deserialize() {
		// not implemented yet
	}
}