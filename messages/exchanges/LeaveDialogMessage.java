package messages.exchanges;

import messages.Message;

public class LeaveDialogMessage extends Message {
	public int dialogType = 0;

	@Override
	public void serialize() {
		// not implemented yet
	}

	@Override
	public void deserialize() {
		this.dialogType = this.content.readByte();
	}
}