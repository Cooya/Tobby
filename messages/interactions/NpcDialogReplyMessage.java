package messages.interactions;

import messages.NetworkMessage;

public class NpcDialogReplyMessage extends NetworkMessage {
	public int replyId = 0;

	@Override
	public void serialize() {
		this.content.writeVarShort(this.replyId);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}