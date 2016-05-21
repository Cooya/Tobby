package messages.fights;

import messages.NetworkMessage;

public class GameActionAcknowledgementMessage extends NetworkMessage {
	public boolean valid = false;
	public int actionId = 0;

	@Override
	public void serialize() {
		this.content.writeBoolean(this.valid);
		this.content.writeByte(this.actionId);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}