package messages.exchange;

import messages.Message;
import utilities.ByteArray;

public class ObjectDeletedMessage extends Message{

	public static final int protocolId = 3024;


	public int objectUID = 0;

	public ObjectDeletedMessage(Message msg)
	{
		super(msg);
	}

	public void deserialize()
	{
		ByteArray buffer= new ByteArray(this.content);
		this.objectUID = buffer.readVarInt();
		if(this.objectUID < 0)
		{
			throw new Error("Forbidden value (" + this.objectUID + ") on element of ObjectDeletedMessage.objectUID.");
		}
	}


}
