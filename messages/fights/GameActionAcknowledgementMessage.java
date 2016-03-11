package messages.fights;

import messages.Message;
import utilities.ByteArray;

public class GameActionAcknowledgementMessage extends Message{

    public boolean valid = false;
    
    public int actionId = 0;
    
    public GameActionAcknowledgementMessage(boolean val,int id)
    {
       super();
       valid=val;
       actionId=id;
    }
    
    public void serialize()
    {
    	ByteArray buffer=new ByteArray();
       buffer.writeBoolean(this.valid);
       buffer.writeByte((byte)this.actionId);
       completeInfos(buffer);
    }
	
}
