package messages.connection;

import messages.Message;
import utilities.ByteArray;

public class CharactersListMessage extends BasicCharactersListMessage {
	public boolean hasStartupActions = false;
	
	public CharactersListMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	public void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		super.deserialize(buffer);
		this.hasStartupActions = buffer.readBoolean();
	}
}