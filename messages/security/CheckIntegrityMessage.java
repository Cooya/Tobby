package messages.security;

import java.util.Vector;

import messages.Message;

public class CheckIntegrityMessage extends Message {
	public Vector<Integer> data;

	@Override
	public void serialize() {
		this.content.writeVarInt(this.data.size());
		for(int i : this.data)
			this.content.writeByte(i);
	}
	
	@Override
	public void deserialize() {
		int nb = this.content.readVarInt();
		this.data = new Vector<Integer>(nb);
		for(int i = 0; i < nb; ++i)
			this.data.add(this.content.readByte());
	}
}