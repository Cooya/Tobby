package messages.game;

import messages.Message;
import utilities.ByteArray;

public class NpcDialogReplyMessage extends Message{
	public static final int Id = 5616;

    public int replyId = 0;
    
    public NpcDialogReplyMessage()
    {
       super();
    }
    
    
    public void AccessToBank(){
    	this.replyId=259;
    }
    
    public void initNpcDialogReplyMessage(int replyId){
    	this.replyId=replyId;
    }

    public void serialize()
    {
       ByteArray buffer=new ByteArray();
       if(this.replyId < 0)
       {
          throw new Error("Forbidden value (" + this.replyId + ") on element replyId.");
       }
       buffer.writeVarShort(this.replyId);
       this.completeInfos(buffer);
    }
    
   
}
