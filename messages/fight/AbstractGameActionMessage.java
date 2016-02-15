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
       if(this.actionId < 0)
       {
          throw new Error("Forbidden value (" + this.actionId + ") on element of AbstractGameActionMessage.actionId.");
       }
       this.sourceId = buffer.readDouble();
       if(this.sourceId < -9.007199254740992E15 || this.sourceId > 9.007199254740992E15)
       {
          throw new Error("Forbidden value (" + this.sourceId + ") on element of AbstractGameActionMessage.sourceId.");
       }
    }
}
