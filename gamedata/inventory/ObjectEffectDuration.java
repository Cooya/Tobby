package gamedata.inventory;

import utilities.ByteArray;

public class ObjectEffectDuration extends ObjectEffect {
	public int days = 0;
	public int hours = 0;
	public int minutes = 0;

	public ObjectEffectDuration(ByteArray buffer) {
		super(buffer);
		this.days = buffer.readVarShort();
		this.hours = buffer.readByte();
		this.minutes = buffer.readByte();
	}
}