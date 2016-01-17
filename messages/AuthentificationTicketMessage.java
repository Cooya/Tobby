package messages;

import utilities.ByteArray;
import utilities.Encryption;

public class AuthentificationTicketMessage extends Message {
	public static final int ID = 110;
	
	public AuthentificationTicketMessage(SelectedServerDataMessage SSDM) {
		super(ID, 0, 0, null);
		serialize(SSDM);
	}
	
	private void serialize(SelectedServerDataMessage SSDM) {
		ByteArray buffer = new ByteArray();
	    buffer.writeUTF("fr");
	    buffer.writeUTF(new String(Encryption.decodeWithAES(ByteArray.toBytes(SSDM.getTicket()))));
	    
	    this.size = buffer.getSize();
	    this.lenofsize = computeLenOfSize(this.size);
	    this.content = buffer.bytes();
	}
}
