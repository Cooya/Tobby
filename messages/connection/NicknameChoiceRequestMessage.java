package messages.connection;

import messages.NetworkMessage;

public class NicknameChoiceRequestMessage extends NetworkMessage {
	public String nickname = "";
	
	@Override
	public void serialize() {
		this.content.writeUTF(this.nickname);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}