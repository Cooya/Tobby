package messages.context;

import java.util.Vector;

import utilities.ByteArray;
import messages.Message;

public class SystemMessageDisplayMessage extends Message {
    public boolean hangUp = false;
    public int msgId = 0;
    public Vector<String> parameters;
    
    public SystemMessageDisplayMessage(Message msg) {
    	super(msg);
    	this.parameters = new Vector<String>();
    	deserialize();
    }
    
    private void deserialize() {
    	ByteArray buffer = new ByteArray(this.content);
    	this.hangUp = buffer.readBoolean();
        this.msgId = buffer.readVarShort();
        int nb = buffer.readShort();
        for(int i = 0; i < nb; ++i)
        	this.parameters.add(buffer.readUTF());
    }
}