package messages.exchange;

import messages.Message;
import utilities.ByteArray;

public class ExchangeAcceptMessage extends Message{
	public static final int protocolId = 5508;
    
    public ExchangeAcceptMessage()
    {
       super();
    }
    
    public void serialize(){
    	ByteArray buffer=new ByteArray();
    	completeInfos(buffer);
    }
    
}
