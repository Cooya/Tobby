package messages.fight;

import messages.Message;
import utilities.ByteArray;

public class GameFightTurnFinishMessage extends Message{
	public static final int Id = 718;
	
	public  GameFightTurnFinishMessage(){
		super();
	}
	
	public void serialize()
    {
		ByteArray buffer=new ByteArray();
		completeInfos(buffer);
    }
	
	
}
