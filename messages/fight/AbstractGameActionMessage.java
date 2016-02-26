package messages.fight;

import messages.Message;
import utilities.ByteArray;

public class AbstractGameActionMessage extends Message{
	public static final int Id = 1000;
    
    public int actionId = 0;
    
    public double sourceId = 0;
    
    public AbstractGameActionMessage(Message msg){
    	super(msg);
    }
    
    public void deserialize(ByteArray buffer)
    {
       this.actionId = buffer.readVarShort();
       this.sourceId = buffer.readDouble();
    }
}
