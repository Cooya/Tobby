package messages.exchange;

import messages.Message;
import utilities.ByteArray;

public class ObjectQuantityMessage extends Message{

	public static final int protocolId = 3023;

	public int objectUID = 0;

	public int quantity = 0;

	public ObjectQuantityMessage(Message msg)
	{
		super(msg);
	}

	public void deserialize()
	{
		ByteArray buffer=new ByteArray(content);
		this.objectUID = buffer.readVarInt();
		this.quantity = buffer.readVarInt();
	}
}
