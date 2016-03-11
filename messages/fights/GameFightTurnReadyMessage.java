package messages.fights;

import messages.Message;
import utilities.ByteArray;

public class GameFightTurnReadyMessage extends Message{

	public boolean isReady = false;

	public GameFightTurnReadyMessage(boolean isReady)
	{
		super();
		this.isReady=isReady;
	}

	public void initGameFightTurnReadyMessage(boolean param1)
	{
		this.isReady = param1;
	}


	public void serialize()
	{
		ByteArray buffer=new ByteArray();
		buffer.writeBoolean(this.isReady);
		completeInfos(buffer);
	}

}
