package messages.connection;

import messages.Message;

public class SelectedServerDataMessage extends Message {
	private int serverId = 0;
	public String address = "";
	public int port = 0;
	public boolean canCreateNewCharacter = false;
	public int[] ticket; // normalement c'est un vecteur
	
	@Override
    public void serialize() {
	    this.content.writeVarShort(this.serverId);
	    this.content.writeUTF(this.address);
	    this.content.writeShort(this.port);
	    this.content.writeBoolean(this.canCreateNewCharacter);
	    this.content.writeVarInt(this.ticket.length);
	    for(int i = 0; i < this.ticket.length; ++i)
	    	this.content.writeByte(this.ticket[i]);
    }

	@Override
	public void deserialize() {
		this.serverId = this.content.readVarShort();
		this.address = this.content.readUTF();
		this.port = this.content.readShort();
		this.canCreateNewCharacter = this.content.readBoolean();
		int ticketSize = this.content.readVarInt();
		this.ticket = new int[ticketSize];
		for(int i = 0; i < ticketSize; ++i)
			this.ticket[i] = this.content.readByte();
	}
}