package gamedata.inventory;

import utilities.ByteArray;

public class ObjectEffectMinMax extends ObjectEffect {
	public int min = 0;
	public int max = 0;

	public ObjectEffectMinMax(ByteArray buffer) {
		super(buffer);
		this.min = buffer.readVarInt();
		this.max = buffer.readVarInt();
	}
}