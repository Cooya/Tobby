package messages.security;

import messages.NetworkMessage;

public class RawDataMessage extends NetworkMessage {
	public byte[] content2;
	
	@Override
	public void serialize() {
		this.content.writeVarInt(this.content2.length);
		this.content.writeBytes(this.content2);
	}
	
	@Override
	public void deserialize() {
		this.content2 = this.content.readBytes(this.content.readVarInt());
	}
}