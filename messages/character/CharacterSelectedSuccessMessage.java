package messages.character;

import game.character.CharacterBaseInformations;
import messages.Message;
import utilities.ByteArray;

public class CharacterSelectedSuccessMessage extends Message{

	public static final int protocolId = 153;
     
    public CharacterBaseInformations infos;
    
    public boolean isCollectingStats = false;
    
    public CharacterSelectedSuccessMessage(Message msg)
    {
    	super(msg);
       
    }
    
    public void deserialize() 
    {
    	ByteArray buffer=new ByteArray(content);
       this.infos = new CharacterBaseInformations(buffer);
       this.isCollectingStats = buffer.readBoolean();
    }
}
