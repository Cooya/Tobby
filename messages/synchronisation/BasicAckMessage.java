package messages.synchronisation;

import utilities.ByteArray;
import messages.Message;

public class BasicAckMessage extends Message {
	public int seq = 0;
	public int lastPacketId = 0;

	public BasicAckMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.seq = buffer.readVarInt();
		this.lastPacketId = buffer.readVarShort();
	}
}