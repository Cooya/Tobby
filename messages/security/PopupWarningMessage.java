package messages.security;

import messages.NetworkMessage;

public class PopupWarningMessage extends NetworkMessage {
	public int lockDuration = 0;
	public String author = "";
	public String content2 = ""; // nom de variable déjà utilisé
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
        this.lockDuration = this.content.readByte();
        this.author = this.content.readUTF();
        this.content2 = this.content.readUTF();
    }
}