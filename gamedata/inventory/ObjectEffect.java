package gamedata.inventory;

import utilities.ByteArray;

public class ObjectEffect 
{


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
	}
}

