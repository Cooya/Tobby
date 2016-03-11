package messages.fights;

import utilities.ByteArray;
import messages.Message;

public class GameFightOptionToggleMessage extends Message {
	public int option = 3;

	public GameFightOptionToggleMessage() {
		super();
	}
	
	public void serialize(int option) {
		this.option = option;
		
		ByteArray buffer = new ByteArray();
		buffer.writeByte(this.option);
		completeInfos(buffer);
	}
}