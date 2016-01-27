package game;

import utilities.ByteArray;

public class ObjectEffectDuration extends ObjectEffect 
{

	public static final int protocolId = 75;

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
		if(this.days < 0)
		{
			throw new Error("Forbidden value (" + this.days + ") on element of ObjectEffectDuration.days.");
		}
		this.hours = buffer.readByte();
		if(this.hours < 0)
		{
			throw new Error("Forbidden value (" + this.hours + ") on element of ObjectEffectDuration.hours.");
		}
		this.minutes = buffer.readByte();
		if(this.minutes < 0)
		{
			throw new Error("Forbidden value (" + this.minutes + ") on element of ObjectEffectDuration.minutes.");
		}
	}
}

