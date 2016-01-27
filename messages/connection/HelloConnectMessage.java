package messages.connection;

import messages.Message;
import utilities.ByteArray;

public class HelloConnectMessage extends Message {
	private String salt;
	private int[] key;
	
	public HelloConnectMessage(Message msg) {
		super(msg);
		
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.salt = buffer.readUTF();
		int keySize = buffer.readVarInt();
		this.key = new int[keySize];
		for(int i = 0; i < keySize; ++i)
			this.key[i] = buffer.readByte();
	}
	
	public void serialize() {
		ByteArray buffer = new ByteArray();
		buffer.writeUTF(this.salt);
		int len = this.key.length;
		buffer.writeVarInt(len);
		for(int i = 0; i < len; ++i)
			buffer.writeByte((byte) this.key[i]);
		
		completeInfos(buffer);
	}
	
	public String getSalt() {
		return this.salt;
	}
	
	public int[] getKey() {
		return this.key;
	}
}
