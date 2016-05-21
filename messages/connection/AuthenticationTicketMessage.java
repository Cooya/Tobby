package messages.connection;

import messages.NetworkMessage;

public class AuthenticationTicketMessage extends NetworkMessage {
	public String lang;
	public String ticket;
	
	@Override
	public void serialize() {
	    this.content.writeUTF(this.lang);
	    this.content.writeUTF(this.ticket);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}