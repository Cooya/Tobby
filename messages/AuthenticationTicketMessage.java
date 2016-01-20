package messages;

import utilities.ByteArray;
import utilities.Encryption;

public class AuthenticationTicketMessage extends Message {
	public static final int ID = 110;
	public AuthenticationTicketMessage() {
		super();
	}
	
	public void serialize(SelectedServerDataMessage SSDM) {
		ByteArray buffer = new ByteArray();
	    buffer.writeUTF("fr");
	    buffer.writeUTF(new String(Encryption.decodeWithAES(ByteArray.toBytes(SSDM.getTicket()))));
	    
	    this.size = buffer.getSize();
	    this.lenofsize = computeLenOfSize(this.size);
	    this.content = buffer.bytes();
	}
}
