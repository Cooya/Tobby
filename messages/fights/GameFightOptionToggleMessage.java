package messages.fights;

import messages.Message;

public class GameFightOptionToggleMessage extends Message {
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