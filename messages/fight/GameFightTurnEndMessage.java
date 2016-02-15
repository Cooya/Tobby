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
        if(this.fighterId < -9.007199254740992E15 || this.fighterId > 9.007199254740992E15)
        {
           throw new Error("Forbidden value (" + this.fighterId + ") on element of GameFightTurnEndMessage.id.");
        }
     }
	
	
}
