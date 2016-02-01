package messages.character;

import messages.Message;
import utilities.ByteArray;

public class InventoryWeightMessage extends Message{


	public static final int Id = 3009;

	public int weight = 0;

	public int weightMax = 0;

	public InventoryWeightMessage(Message msg)
	{
		super();
		ByteArray buffer=new ByteArray(msg.getContent());
		deserialize(buffer);
	}

	public void reset()
	{
		this.weight = 0;
		this.weightMax = 0;
	}

	public void deserialize(ByteArray buffer) 
	{
		this.weight = buffer.readVarInt();
		if(this.weight < 0)
		{
			throw new Error("Forbidden value (" + this.weight + ") on element of InventoryWeightMessage.weight.");
		}
		this.weightMax = buffer.readVarInt();
		if(this.weightMax < 0)
		{
			throw new Error("Forbidden value (" + this.weightMax + ") on element of InventoryWeightMessage.weightMax.");
		}
	}
}



