package messages.fights;

import messages.Message;

public class GameFightTurnReadyRequestMessage extends Message {
	public double id = 0;

	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		this.id = this.content.readDouble();
	}
}