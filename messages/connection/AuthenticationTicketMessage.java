package messages.connection;

import messages.Message;
import utilities.ByteArray;
import utilities.Encryption;

public class AuthenticationTicketMessage extends Message {
	public static final int ID = 110;
	public AuthenticationTicketMessage() {
		super();
	}
	
	public void serialize(int[] ticket) {
		ByteArray buffer = new ByteArray();
	    buffer.writeUTF("fr");
	    buffer.writeUTF(new String(Encryption.decodeWithAES(ByteArray.toBytes(ticket))));
	    
	    completeInfos(buffer);
	}
}
