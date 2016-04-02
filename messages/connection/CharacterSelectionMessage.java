package messages.connection;

import messages.Message;
import utilities.ByteArray;

public class CharacterSelectionMessage extends Message {
	public double id = 0;
	
	public CharacterSelectionMessage() {
		super();
	}
	
	public void serialize(ByteArray buffer) {
		buffer.writeVarLong(this.id);
	}
	
	public void serialize() {
		ByteArray buffer = new ByteArray();
		buffer.writeVarLong(this.id);
		super.completeInfos(buffer);
	}
}