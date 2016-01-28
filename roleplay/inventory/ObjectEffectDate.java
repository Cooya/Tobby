package roleplay.inventory;

import utilities.ByteArray;

public class ObjectEffectDate extends ObjectEffect
{

	public static final int protocolId = 72;

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
		if(this.year < 0)
		{
			throw new Error("Forbidden value (" + this.year + ") on element of ObjectEffectDate.year.");
		}
		this.month = buffer.readByte();
		if(this.month < 0)
		{
			throw new Error("Forbidden value (" + this.month + ") on element of ObjectEffectDate.month.");
		}
		this.day = buffer.readByte();
		if(this.day < 0)
		{
			throw new Error("Forbidden value (" + this.day + ") on element of ObjectEffectDate.day.");
		}
		this.hour = buffer.readByte();
		if(this.hour < 0)
		{
			throw new Error("Forbidden value (" + this.hour + ") on element of ObjectEffectDate.hour.");
		}
		this.minute = buffer.readByte();
		if(this.minute < 0)
		{
			throw new Error("Forbidden value (" + this.minute + ") on element of ObjectEffectDate.minute.");
		}
	}
}

