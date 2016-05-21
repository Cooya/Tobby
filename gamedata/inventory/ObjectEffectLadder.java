package gamedata.inventory;

import utilities.ByteArray;

public class ObjectEffectLadder extends ObjectEffectCreature {
	public int monsterCount = 0;

	public ObjectEffectLadder(ByteArray buffer) {
		super(buffer);
		this.monsterCount = buffer.readVarInt();
	}
}