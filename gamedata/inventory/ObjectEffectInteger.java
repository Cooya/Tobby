package gamedata.inventory;

import utilities.ByteArray;

public class ObjectEffectInteger extends ObjectEffect {
	public int value = 0;

	public ObjectEffectInteger(ByteArray buffer) {
		super(buffer);
		this.value = buffer.readVarShort();
	}
}