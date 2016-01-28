package roleplay.inventory;

import utilities.ByteArray;

public class ObjectEffectLadder extends ObjectEffectCreature
{

	public static final int protocolId = 81;

	public int monsterCount = 0;

	public ObjectEffectLadder()
	{
		super();
	}

	public int getTypeId() 
	{
		return 81;
	}

	public ObjectEffectLadder initObjectEffectLadder(int param1,int param2,int param3)
	{
		super.initObjectEffectCreature(param1,param2);
		this.monsterCount = param3;
		return this;
	}

	public void reset()
	{
		super.reset();
		this.monsterCount = 0;
	}

	public void deserialize(ByteArray buffer)
	{
		super.deserialize(buffer);
		this.monsterCount = buffer.readVarInt();
		if(this.monsterCount < 0)
		{
			throw new Error("Forbidden value (" + this.monsterCount + ") on element of ObjectEffectLadder.monsterCount.");
		}
	}
}
