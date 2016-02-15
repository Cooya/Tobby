package messages.connection;

import messages.Message;
import utilities.ByteArray;
import utilities.Encryption;

public class AuthenticationTicketMessage extends Message {
	private final String lang = "fr";
	private String ticket;
	
	public AuthenticationTicketMessage() {
		super();
	}
	
	public void serialize(int[] ticket) {
		this.ticket = new String(Encryption.decodeWithAES(ByteArray.toBytes(ticket)));
		ByteArray buffer = new ByteArray();
	    buffer.writeUTF(lang);
	    buffer.writeUTF(this.ticket);
	    
	    completeInfos(buffer);
	}
}
