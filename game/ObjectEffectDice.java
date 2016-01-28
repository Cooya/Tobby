package game;

import utilities.ByteArray;

public class ObjectEffectDice extends ObjectEffect
{

	public static final int protocolId = 73;

	public int diceNum = 0;

	public int diceSide = 0;

	public int diceConst = 0;

	public ObjectEffectDice()
	{
		super();
	}

	public int getTypeId()
	{
		return 73;
	}

	public ObjectEffectDice initObjectEffectDice( int buffer,int param2,int param3,int param4) 
	{
		super.initObjectEffect(buffer);
		this.diceNum = param2;
		this.diceSide = param3;
		this.diceConst = param4;
		return this;
	}

	public void reset()
	{
		super.reset();
		this.diceNum = 0;
		this.diceSide = 0;
		this.diceConst = 0;
	}



	public void deserialize(ByteArray buffer) 
	{
		super.deserialize(buffer);
		this.diceNum = buffer.readVarShort();
		if(this.diceNum < 0)
		{
			throw new Error("Forbidden value (" + this.diceNum + ") on element of ObjectEffectDice.diceNum.");
		}
		this.diceSide = buffer.readVarShort();
		if(this.diceSide < 0)
		{
			throw new Error("Forbidden value (" + this.diceSide + ") on element of ObjectEffectDice.diceSide.");
		}
		this.diceConst = buffer.readVarShort();
		if(this.diceConst < 0)
		{
			throw new Error("Forbidden value (" + this.diceConst + ") on element of ObjectEffectDice.diceConst.");
		}
	}
}
