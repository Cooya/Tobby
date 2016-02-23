package messages.exchange;

import utilities.ByteArray;
import messages.Message;

public class LeaveDialogMessage extends Message {
	public int dialogType = 0;

	public LeaveDialogMessage(Message msg) {
		super(msg);
	}
	
	protected void deserialize(ByteArray buffer) {
		this.dialogType = buffer.readByte();
	}
}