package messages.exchange;

import utilities.ByteArray;
import messages.Message;

public class ExchangeLeaveMessage extends LeaveDialogMessage {
	public boolean success = false;
	
	public ExchangeLeaveMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		super.deserialize(buffer);
		this.success = buffer.readBoolean();
	}
}