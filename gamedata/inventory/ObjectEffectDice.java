package gamedata.inventory;

import utilities.ByteArray;

public class ObjectEffectDice extends ObjectEffect {
	public int diceNum = 0;
	public int diceSide = 0;
	public int diceConst = 0;

	public ObjectEffectDice(ByteArray buffer) {
		super(buffer);
		this.diceNum = buffer.readVarShort();
		this.diceSide = buffer.readVarShort();
		this.diceConst = buffer.readVarShort();
	}
}