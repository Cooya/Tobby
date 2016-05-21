package messages.character;

import messages.NetworkMessage;

public class BasicWhoIsRequestMessage extends NetworkMessage {
	public boolean verbose = false;
	public String search = "";
	
	@Override
	public void serialize() {
		this.content.writeBoolean(this.verbose);
		this.content.writeUTF(this.search);
	}

	@Override
	public void deserialize() {
		// not implemented yet
	}
}