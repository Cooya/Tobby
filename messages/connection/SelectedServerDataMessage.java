package messages.connection;

import messages.Message;
import utilities.ByteArray;

public class SelectedServerDataMessage extends Message {
	private int serverId;
    public String address;
    public int port;
    public boolean canCreateNewCharacter = false;
    public int[] ticket;
	
    public SelectedServerDataMessage(Message msg) {
    	super(msg);
    	deserialize();
    }
    
    private void deserialize() {
    	ByteArray buffer = new ByteArray(this.content);
    	this.serverId = buffer.readVarShort();
    	this.address = buffer.readUTF();
    	this.port = buffer.readShort();
    	this.canCreateNewCharacter = buffer.readBoolean();
    	int ticketSize = buffer.readVarInt();
    	ticket = new int[ticketSize];
    	for(int i = 0; i < ticketSize; ++i)
    		ticket[i] = buffer.readByte();
    }
    
    public void serialize(String localAddress) {
    	this.address = localAddress;
    	
		ByteArray buffer = new ByteArray();
	    buffer.writeVarShort(this.serverId);
	    buffer.writeUTF(this.address);
	    buffer.writeShort((short) this.port);
	    buffer.writeBoolean(this.canCreateNewCharacter);
	    buffer.writeVarInt(this.ticket.length);
	    for(int i = 0; i < this.ticket.length; ++i)
	    	buffer.writeByte((byte) this.ticket[i]);
	    
	    completeInfos(buffer);
    }
}
