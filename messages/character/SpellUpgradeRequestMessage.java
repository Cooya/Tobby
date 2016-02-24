package messages.character;

import messages.Message;
import utilities.ByteArray;

public class SpellUpgradeRequestMessage extends Message{
	
	public static final int protocolId = 5608;
    
    public int spellId = 0;
    
    public int spellLevel = 0;
    
    public SpellUpgradeRequestMessage()
    {
       super();
    }
    
   
    public void serialize(int id,int lvl)
    {
    	ByteArray buffer =new ByteArray();
       buffer.writeVarShort(id);
       buffer.writeByte(lvl);
       this.completeInfos(buffer);
    }

}
