package messages.interactions;

import messages.Message;

public class NpcDialogReplyMessage extends Message {
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