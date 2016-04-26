package messages.security;

import messages.Message;

public class CheckFileRequestMessage extends Message {
	public String filename = "";
	public int type = 0;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		this.filename = this.content.readUTF();
		this.type = this.content.readByte();
	}
}