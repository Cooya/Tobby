package messages.character;

import messages.Message;
import utilities.ByteArray;

public class CharacterLevelUpMessage extends Message{
	public static final int protocolId = 5670;
    
    public int newLevel = 0;
    
    public CharacterLevelUpMessage(Message msg)
    {
       super(msg);
    }
    
    public void deserialize() 
    {
    	ByteArray buffer=new ByteArray(this.content);
       this.newLevel = buffer.readByte();
    }

}
