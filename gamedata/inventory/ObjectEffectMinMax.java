package gamedata.inventory;

import utilities.ByteArray;

public class ObjectEffectMinMax extends ObjectEffect 
{

	public int min = 0;

	public int max = 0;

	public ObjectEffectMinMax()
	{
		super();
	}

	public int getTypeId() 
	{
		return 82;
	}

	public ObjectEffectMinMax initObjectEffectMinMax(int buffer,int param2,int param3) 
	{
		super.initObjectEffect(buffer);
		this.min = param2;
		this.max = param3;
		return this;
	}

	public void reset() 
	{
		super.reset();
		this.min = 0;
		this.max = 0;
	}

	public void deserialize(ByteArray buffer)
	{
		super.deserialize(buffer);
		this.min = buffer.readVarInt();
		this.max = buffer.readVarInt();
	}
}
