package messages.fight;

import messages.Message;
import utilities.ByteArray;

public class GameFightTurnReadyRequestMessage extends Message{

	
	
	public static final int Id = 715;
    

    public double id = 0;
    
    public GameFightTurnReadyRequestMessage(Message msg)
    {
       super();
       ByteArray buffer=new ByteArray(msg.getContent());
       deserialize(buffer);
    }
    
  
    public void deserialize(ByteArray buffer) 
    {
       this.id = buffer.readDouble();
    }
}
