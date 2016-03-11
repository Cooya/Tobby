package messages.fights;

import messages.Message;
import utilities.ByteArray;

public class GameFightReadyMessage extends Message{

	public GameFightReadyMessage()
	{
		super();
	}

	public void serialize()
	{
		ByteArray buffer=new ByteArray();
		buffer.writeBoolean(true);
		completeInfos(buffer);
	}


}
