package messages.synchronisation;

import messages.NetworkMessage;

public class BasicPingMessage extends NetworkMessage {
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