package messages.fight;

import messages.Message;
import utilities.ByteArray;

public class GameFightTurnEndMessage extends Message{

	public static final int Id = 719;
	
	public double fighterId;
	
	public GameFightTurnEndMessage(Message msg)
	{
		super(msg);
	}
	
	 public void deserialize()
     {
		 ByteArray buffer=new ByteArray(this.getContent());
        this.fighterId = buffer.readDouble();
     }
	
	
}
