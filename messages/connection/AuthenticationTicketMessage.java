package messages.connection;

import messages.Message;

public class AuthenticationTicketMessage extends Message {
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