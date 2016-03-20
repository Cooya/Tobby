package messages.security;

import utilities.ByteArray;
import messages.Message;

public class PopupWarningMessage extends Message {
	public int lockDuration = 0;
	public String author = "";
	public String content = "";
	
	public PopupWarningMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(super.content);
        this.lockDuration = buffer.readByte();
        this.author = buffer.readUTF();
        this.content = buffer.readUTF();
    }
}