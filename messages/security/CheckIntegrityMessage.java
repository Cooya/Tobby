package messages.security;

import messages.NetworkMessage;

public class CheckIntegrityMessage extends NetworkMessage {
	public int[] data;

	@Override
	public void serialize() {
		this.content.writeVarInt(this.data.length);
		for(int i : this.data)
			this.content.writeByte(i);
	}
	
	@Override
	public void deserialize() {
		int nb = this.content.readVarInt();
		this.data = new int[nb];
		for(int i = 0; i < nb; ++i)
			this.data[i] = this.content.readByte();
	}
}