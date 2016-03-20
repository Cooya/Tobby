package messages.security;

import utilities.ByteArray;
import messages.Message;

public class CheckFileRequestMessage extends Message {
	public String filename = "";
	public int type = 0;
	
	public CheckFileRequestMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.filename = buffer.readUTF();
		this.type = buffer.readByte();
	}
}