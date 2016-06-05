package messages.context;

import messages.NetworkMessage;

public class SystemMessageDisplayMessage extends NetworkMessage {
    public boolean hangUp = false;
    public int msgId = 0;
    public String[] parameters;
    
    @Override
	public void serialize() {
		// not implemented yet
	}
    
    @Override
    public void deserialize() {
    	this.hangUp = this.content.readBoolean();
        this.msgId = this.content.readVarShort();
        int nb = this.content.readShort();
        this.parameters = new String[nb];
        for(int i = 0; i < nb; ++i)
        	this.parameters[i] = this.content.readUTF();
    }
}