package game.inventory;

import utilities.ByteArray;

public class ObjectEffect 
{

	public static final int  protocolId = 76;

	public int actionId = 0;

	public ObjectEffect()
	{
	}

	public int getTypeId() 
	{
		return 76;
	}

	public ObjectEffect initObjectEffect(int param1) 
	{
		this.actionId = param1;
		return this;
	}

	public void reset()
	{
		this.actionId = 0;
	}



	public void deserialize(ByteArray buffer)
	{
		this.actionId = buffer.readVarShort();
		if(this.actionId < 0)
		{
			throw new Error("Forbidden value (" + this.actionId + ") on element of ObjectEffect.actionId.");
		}
	}
}

