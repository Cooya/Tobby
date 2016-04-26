package messages.security;

import messages.Message;

public class CheckFileMessage extends Message {
	public String filenameHash = "";
	public int type = 0;
	public String value = "";
	
	@Override
	public void serialize() {
		this.content.writeUTF(this.filenameHash);
		this.content.writeByte(this.type);
		this.content.writeUTF(this.value);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}