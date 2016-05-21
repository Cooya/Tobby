package messages.context;

import java.util.Vector;

import messages.NetworkMessage;

public class SystemMessageDisplayMessage extends NetworkMessage {
    public boolean hangUp = false;
    public int msgId = 0;
    public Vector<String> parameters;
    
    @Override
	public void serialize() {
		// not implemented yet
	}
    
    @Override
    public void deserialize() {
    	this.parameters = new Vector<String>();
    	this.hangUp = this.content.readBoolean();
        this.msgId = this.content.readVarShort();
        int nb = this.content.readShort();
        for(int i = 0; i < nb; ++i)
        	this.parameters.add(this.content.readUTF());
    }
}