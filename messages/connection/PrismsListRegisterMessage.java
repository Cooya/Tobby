package messages.connection;

import messages.Message;

public class PrismsListRegisterMessage extends Message {
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