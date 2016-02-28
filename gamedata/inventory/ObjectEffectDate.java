package gamedata.inventory;

import utilities.ByteArray;

public class ObjectEffectDate extends ObjectEffect
{

	public int year = 0;

	public int month = 0;

	public int day = 0;

	public int hour = 0;

	public int minute = 0;

	public ObjectEffectDate()
	{
		super();
	}

	public int getTypeId() 
	{
		return 72;
	}

	public ObjectEffectDate initObjectEffectDate(int buffer,int param2,int param3,int param4,int param5,int param6) 
	{
		super.initObjectEffect(buffer);
		this.year = param2;
		this.month = param3;
		this.day = param4;
		this.hour = param5;
		this.minute = param6;
		return this;
	}

	public void reset()
	{
		super.reset();
		this.year = 0;
		this.month = 0;
		this.day = 0;
		this.hour = 0;
		this.minute = 0;
	}

	public void deserializeAs_ObjectEffectDate(ByteArray buffer)
	{
		super.deserialize(buffer);
		this.year = buffer.readVarShort();
		this.month = buffer.readByte();
		this.day = buffer.readByte();
		this.hour = buffer.readByte();
		this.minute = buffer.readByte();
	}
}

