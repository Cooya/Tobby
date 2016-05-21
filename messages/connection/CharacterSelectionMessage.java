package messages.connection;

import messages.NetworkMessage;

public class CharacterSelectionMessage extends NetworkMessage {
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