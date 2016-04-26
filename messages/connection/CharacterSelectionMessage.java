package messages.connection;

import messages.Message;

public class CharacterSelectionMessage extends Message {
	public double id = 0;
	
	@Override
	public void serialize() {
		this.content.writeVarLong(this.id);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}