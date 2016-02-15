package messages.connection;

import messages.Message;
import utilities.ByteArray;
import utilities.Encryption;

public class AuthenticationTicketMessage extends Message {
	public String lang;
	public String ticket;
	
	public AuthenticationTicketMessage() {
		super();
	}
	
	public void serialize(String lang, int[] ticket) {
		this.lang = lang;
		this.ticket = new String(Encryption.decodeWithAES(ByteArray.toBytes(ticket)));
		ByteArray buffer = new ByteArray();
	    buffer.writeUTF(this.lang);
	    buffer.writeUTF(this.ticket);
	    
	    completeInfos(buffer);
	}
}