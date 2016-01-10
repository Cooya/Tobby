package messages;

import utilities.ByteArray;

public class AuthentificationTicketMessage extends Message {
	private static final short id = 110;
	
	private int serverId;
	private char[] address;
	private int port;
	private boolean canCreateNewCharacter;
	private byte[] ticket;
	private int[] serverIds;
	
	public AuthentificationTicketMessage(byte[] content) {
		super(id, (short) 0, 0, null);
		deserializeSSDEM(content);
		serializeATM(content);
	}

	private void deserializeSSDEM(byte[] content) {
		ByteArray array = new ByteArray(content);
		this.serverId = array.readVarShort();
		this.address = array.readUTF();
		this.port = array.readShort();
		this.canCreateNewCharacter = array.readBoolean();
		this.ticket = array.readBytes(array.readVarInt());
		
		short counter = array.readShort();
		for(int i = 0; i < counter; ++i)
			this.serverIds[i] = array.readVarShort();
	}
	
	private void serializeATM(byte[] content) {
		ByteArray buffer = new ByteArray(content);
	    buffer.writeUTF("fr".toCharArray());
	    buffer.writeUTF(this.ticket);
	    
	    this.size = buffer.getSize();
	    this.lenofsize = computeLenOfSize(this.size);
	    this.content = buffer.bytes();
	}
	
	public char[] getIP() {
		return this.address;
	}
}
