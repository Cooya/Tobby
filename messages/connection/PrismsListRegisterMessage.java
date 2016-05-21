package messages.connection;

import messages.NetworkMessage;

public class PrismsListRegisterMessage extends NetworkMessage {
	public int listen;

	@Override
	public void serialize() {
		this.content.writeByte(this.listen); 
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}