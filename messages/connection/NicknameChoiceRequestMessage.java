package messages.connection;

import messages.Message;

public class NicknameChoiceRequestMessage extends Message {
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