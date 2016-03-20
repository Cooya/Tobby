package messages.security;

import utilities.ByteArray;
import messages.Message;

public class CheckFileMessage extends Message {
	public String filenameHash = "";
	public int type = 0;
	public String value = "";
	
	public CheckFileMessage() {
		super();
	}
	
	public void serialize() {
		ByteArray buffer = new ByteArray();
		buffer.writeUTF(this.filenameHash);
		buffer.writeByte(this.type);
		buffer.writeUTF(this.value);
	}
}