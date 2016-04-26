package messages.fights;

import messages.Message;
import utilities.ByteArray;

public class GameActionAcknowledgementMessage extends Message {
	public boolean valid = false;
	public int actionId = 0;

	public GameActionAcknowledgementMessage() {
		super();
	}

	public void serialize(boolean valid, int actionId) {
		this.valid = valid;
		this.actionId = actionId;
		ByteArray buffer = new ByteArray();
		buffer.writeBoolean(this.valid);
		buffer.writeByte(this.actionId);
		super.completeInfos(buffer);
	}
}