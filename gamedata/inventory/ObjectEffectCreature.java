package gamedata.inventory;

import utilities.ByteArray;

public class ObjectEffectCreature extends ObjectEffect {
	public int monsterFamilyId = 0;

	public ObjectEffectCreature(ByteArray buffer) {
		super(buffer);
		this.monsterFamilyId = buffer.readVarShort();
	}
}