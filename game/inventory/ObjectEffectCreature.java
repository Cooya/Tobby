package game.inventory;

import utilities.ByteArray;

public class ObjectEffectCreature extends ObjectEffect 
{

	public static final int protocolId = 71;

	public int monsterFamilyId = 0;

	public ObjectEffectCreature()
	{
		super();
	}

	public int getTypeId()
	{
		return 71;
	}

	public ObjectEffectCreature initObjectEffectCreature(int buffer,int param2)
	{
		super.initObjectEffect(buffer);
		this.monsterFamilyId = param2;
		return this;
	}

	public void reset()
	{
		super.reset();
		this.monsterFamilyId = 0;
	}


	public void deserializeAs_ObjectEffectCreature(ByteArray buffer) 
	{
		super.deserialize(buffer);
		this.monsterFamilyId = buffer.readVarShort();
		if(this.monsterFamilyId < 0)
		{
			throw new Error("Forbidden value (" + this.monsterFamilyId + ") on element of ObjectEffectCreature.monsterFamilyId.");
		}
	}
}
