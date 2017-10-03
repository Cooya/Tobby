package messages.security;

import messages.NetworkMessage;

public class RawDataMessage extends NetworkMessage {
	public byte[] _content;
	
	@Override
	public void serialize() {
		this.content.writeVarInt(this._content.length);
		this.content.writeBytes(this._content);
	}
	
	@Override
	public void deserialize() {
		this._content = this.content.readBytes(this.content.readVarInt());
	}
}