package messages.character;

import messages.NetworkMessage;

public class BasicWhoIsNoMatchMessage extends NetworkMessage {
	public String search = "";

	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		this.search = this.content.readUTF();
	}
}