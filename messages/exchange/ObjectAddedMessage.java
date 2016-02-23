package messages.exchange;

import messages.Message;
import gamedata.inventory.ObjectItem;
import utilities.ByteArray;

public class ObjectAddedMessage extends Message{
	
	  public static final int protocolId = 3025;
      
      public ObjectItem object;
      
      public ObjectAddedMessage(Message msg)
      {
         super(msg);
      }
      
      
      public void deserializeAs_ObjectAddedMessage() 
      {
    	  ByteArray buffer=new ByteArray(this.content);
    	  this.object = new ObjectItem(buffer);
      }
	

}
