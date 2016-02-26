package gamedata.inventory;

import utilities.ByteArray;

public class ObjectEffectMount extends ObjectEffect
{

	public static final int protocolId = 179;

	public int mountId = 0;

	public double date = 0;

	public int modelId = 0;

	public ObjectEffectMount()
	{
		super();
	}

	public int getTypeId() 
	{
		return 179;
	}

	public ObjectEffectMount initObjectEffectMount(int buffer,int param2,int param3 ,int param4) 
	{
		super.initObjectEffect(buffer);
		this.mountId = param2;
		this.date = param3;
		this.modelId = param4;
		return this;
	}

	public void reset() 
	{
		super.reset();
		this.mountId = 0;
		this.date = 0;
		this.modelId = 0;
	}


	public void deserialize(ByteArray buffer) 
	{
		super.deserialize(buffer);
		this.mountId = buffer.readInt();
		this.date = buffer.readDouble();
		this.modelId = buffer.readVarShort();
	}
}
