package messages.exchange;

import messages.Message;
import utilities.ByteArray;

public class ExchangeReadyMessage extends Message{
	
	public static final int protocolId = 5511;
    
    public ExchangeReadyMessage()
    {
       super();
    }
    
   public void serialize(boolean ready,short step)
    {
	   ByteArray buffer=new ByteArray();
       buffer.writeBoolean(ready);
       buffer.writeVarShort(step);
       completeInfos(buffer);
    }
}
