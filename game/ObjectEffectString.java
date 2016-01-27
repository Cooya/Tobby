package game;

import utilities.ByteArray;

public class ObjectEffectString extends ObjectEffect
{

	public static final int protocolId = 74;

	public String value = "";

	public ObjectEffectString()
	{
		super();
	}

	public int getTypeId()
	{
		return 74;
	}

	public ObjectEffectString initObjectEffectString(int param1,String param2)
	{
		super.initObjectEffect(param1);
		this.value = param2;
		return this;
	}

	public void reset()
	{
		super.reset();
		this.value = "";
	}

	public void deserialize(ByteArray buffer)
	{
		super.deserialize(buffer);
		this.value = buffer.readUTF();
	}
}

