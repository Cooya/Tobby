package messages.exchange;

import messages.Message;
import utilities.ByteArray;

public class ExchangeObjectTransfertAllFromInvMessage extends Message{

	public static final int protocolId = 6184;

	public ExchangeObjectTransfertAllFromInvMessage()
	{
		super();
	}


	public void serialize(){
		ByteArray buffer=new ByteArray();
		completeInfos(buffer);
	}

}
