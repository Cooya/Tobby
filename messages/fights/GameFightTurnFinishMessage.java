package messages.fights;

import messages.Message;
import utilities.ByteArray;

public class GameFightTurnFinishMessage extends Message{
	
	public  GameFightTurnFinishMessage(){
		super();
	}
	
	public void serialize()
    {
		ByteArray buffer=new ByteArray();
		completeInfos(buffer);
    }
	
	
}
