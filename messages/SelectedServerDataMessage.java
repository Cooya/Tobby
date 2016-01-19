package messages;

import utilities.ByteArray;

public class SelectedServerDataMessage extends Message {
	public static final int ID = 42;
    @SuppressWarnings("unused")
	private int serverId = 0;
    public String address = "";
    public int port = 0;
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
    
    public int[] getTicket() {
    	return this.ticket;
    }
    
    public String getAddress() {
    	return this.address;
    }
}
