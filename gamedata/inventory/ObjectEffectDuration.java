package gamedata.inventory;

import utilities.ByteArray;

public class ObjectEffectDuration extends ObjectEffect 
{

	public int days = 0;

	public int hours = 0;

	public int minutes = 0;

	public ObjectEffectDuration()
	{
		super();
	}

	public int getTypeId() 
	{
		return 75;
	}

	public ObjectEffectDuration initObjectEffectDuration(int buffer,int param2,int param3,int param4) 
	{
		super.initObjectEffect(buffer);
		this.days = param2;
		this.hours = param3;
		this.minutes = param4;
		return this;
	}

	public void reset()
	{
		super.reset();
		this.days = 0;
		this.hours = 0;
		this.minutes = 0;
	}

	public void deserialize(ByteArray buffer)
	{
		super.deserialize(buffer);
		this.days = buffer.readVarShort();
		this.hours = buffer.readByte();
		this.minutes = buffer.readByte();
	}
}

