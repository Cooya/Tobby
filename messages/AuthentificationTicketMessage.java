package messages;

import utilities.ByteArray;

public class AuthentificationTicketMessage extends Message {
	private static final short id = 110;
	private String IP;
	private char[] ticket;
	
	public AuthentificationTicketMessage(byte[] content) {
		super(id, (short) 0, 0, null);
		deserializeSSDEM(content);
		serializeATM(content);
	}

	private void deserializeSSDEM(byte[] content) {
		ByteArray array = new ByteArray(content);
		array.readBytes(2); // octets inutiles
		this.IP = array.readUTF().toString();
		array.readShort();
		this.ticket = array.readUTF();
	}
	
	private void serializeATM(byte[] content) {
		ByteArray buffer = new ByteArray(content);
	    buffer.writeUTF("fr".toCharArray());
	    buffer.writeUTF(this.ticket);
	    
	    this.size = buffer.getSize();
	    this.lenofsize = computeLenOfSize(this.size);
	    this.content = buffer.bytes();
	}
	
	public String getIP() {
		return this.IP;
	}
}
