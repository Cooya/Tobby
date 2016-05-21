package messages.synchronisation;

import messages.NetworkMessage;

public class BasicAckMessage extends NetworkMessage {
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