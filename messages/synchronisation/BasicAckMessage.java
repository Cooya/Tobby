package messages.synchronisation;

import messages.Message;

public class BasicAckMessage extends Message {
	public int seq = 0;
	public int lastPacketId = 0;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		this.seq = this.content.readVarInt();
		this.lastPacketId = this.content.readVarShort();
	}
}