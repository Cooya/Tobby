package messages.fight;

import messages.Message;
import utilities.ByteArray;

public class SequenceEndMessage extends Message{
	public static final int Id = 956;
    
    public int actionId = 0;
    
    public double authorId = 0;
    
    public int sequenceType = 0;
    
    public SequenceEndMessage(Message msg)
    {
       super(msg);
    }
    
    public void deserialize()
    {
    	ByteArray buffer=new ByteArray(this.getContent());
       this.actionId = buffer.readVarShort();
       if(this.actionId < 0)
       {
          throw new Error("Forbidden value (" + this.actionId + ") on element of SequenceEndMessage.actionId.");
       }
       this.authorId = buffer.readDouble();
       if(this.authorId < -9.007199254740992E15 || this.authorId > 9.007199254740992E15)
       {
          throw new Error("Forbidden value (" + this.authorId + ") on element of SequenceEndMessage.authorId.");
       }
       this.sequenceType = buffer.readByte();
    }
}
