package messages.context;

import messages.NetworkMessage;

public class LeaveDialogMessage extends NetworkMessage {
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