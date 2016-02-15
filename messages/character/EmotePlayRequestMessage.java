package messages.character;

import messages.Message;
import utilities.ByteArray;

public class EmotePlayRequestMessage extends Message{

	public static final int Id = 5685;

	public int emoteId = 0;

	public EmotePlayRequestMessage()
	{
		super();
	}

	public void serialize(byte emoteId) 
	{
		ByteArray buffer=new ByteArray();
		if(this.emoteId < 0 || this.emoteId > 255)
		{
			throw new Error("Forbidden value (" + this.emoteId + ") on element emoteId.");
		}
		buffer.writeByte(emoteId);
		completeInfos(buffer);
	}
}
