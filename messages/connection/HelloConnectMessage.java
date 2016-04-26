package messages.connection;

import messages.Message;

public class HelloConnectMessage extends Message {
	public String salt;
	public int[] key; // normalement c'est un vecteur
	
	@Override
	public void serialize() {
		this.content.writeUTF(this.salt);
		int len = this.key.length;
		this.content.writeVarInt(len);
		for(int i = 0; i < len; ++i)
			this.content.writeByte(this.key[i]);
	}
	
	@Override
	public void deserialize() {
		this.salt = this.content.readUTF();
		int keySize = this.content.readVarInt();
		this.key = new int[keySize];
		for(int i = 0; i < keySize; ++i)
			this.key[i] = this.content.readByte();
	}
}