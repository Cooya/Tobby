package messages.character;

import messages.Message;
import utilities.ByteArray;

public class NpcGenericActionRequestMessage extends Message{

	public static final int Id = 5898;

	public int npcId = 0;

	public int npcActionId = 0;

	public int npcMapId = 0;

	public NpcGenericActionRequestMessage()
	{
		super();
	}

	public void initNpcGenericActionRequestMessage(int npcId,int npcActionId,int npcMapId)
	{
		this.npcId = npcId;
		this.npcActionId = npcActionId;
		this.npcMapId = npcMapId;
	}
	
	public void SpeakBanker(){
		this.npcId=-10001;
		this.npcActionId=3;
		this.npcMapId=83887104;
	}

	public void serialize() 
    {
		ByteArray buffer=new ByteArray();
       buffer.writeInt(this.npcId);
       if(this.npcActionId < 0)
       {
          throw new Error("Forbidden value (" + this.npcActionId + ") on element npcActionId.");
       }
       buffer.writeByte((byte) this.npcActionId);
       buffer.writeInt(this.npcMapId);
       this.completeInfos(buffer);
    }

}




