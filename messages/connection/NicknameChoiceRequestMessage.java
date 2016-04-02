package messages.connection;

import utilities.ByteArray;
import messages.Message;

public class NicknameChoiceRequestMessage extends Message {
	public String nickname = "";
	
	public NicknameChoiceRequestMessage() {
		super();
	}
	
	public void serialize() {
		ByteArray buffer = new ByteArray();
		buffer.writeUTF(this.nickname);
		super.completeInfos(buffer);
	}
}