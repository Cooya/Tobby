package messages.exchange;

import messages.Message;
import utilities.ByteArray;
import utilities.Int64;

public class ExchangePlayerRequestMessage extends Message{
	public static final int protocolId = 5773;

	public double target;
	
	public int type = 1;

	public ExchangePlayerRequestMessage()
	{
		super();
	}

	public void serialize(double target) 
	{
		ByteArray buffer=new ByteArray();
		buffer.writeByte((byte) this.type);
		buffer.writeVarLong(Int64.fromNumber(target));
		completeInfos(buffer);
	}

}
