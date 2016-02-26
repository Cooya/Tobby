package gamedata.inventory;

import utilities.ByteArray;

public class ObjectEffectInteger extends ObjectEffect{

	public static final int protocolId = 70;

	public int value = 0;

	public ObjectEffectInteger()
	{
		super();
		value=0;
	}

	public int getTypeId()
	{
		return 70;
	}

	public ObjectEffectInteger initObjectEffectInteger(int param1, int param2)
	{
		super.initObjectEffect(param1);
		this.value = param2;
		return this;
	}

	public void  reset()
	{
		super.reset();
		this.value = 0;
	}



	public void deserialize(ByteArray buffer) 
	{
		super.deserialize(buffer);
		this.value = buffer.readVarShort();
	}

}


