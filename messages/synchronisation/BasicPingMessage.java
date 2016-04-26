package messages.synchronisation;

import messages.Message;

public class BasicPingMessage extends Message {
	public boolean quiet = false;
	
	@Override
	public void serialize() {
		this.content.writeBoolean(this.quiet);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}