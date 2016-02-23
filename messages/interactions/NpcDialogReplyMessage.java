package messages.interactions;

import messages.Message;
import utilities.ByteArray;

public class NpcDialogReplyMessage extends Message {
	public int replyId = 0;

	public NpcDialogReplyMessage() {
		super();
	}

	public void serialize(int replyId) {
		this.replyId = replyId;

		ByteArray buffer = new ByteArray();
		buffer.writeVarShort(this.replyId);
		this.completeInfos(buffer);
	}
}