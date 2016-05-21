package gamedata.inventory;

import utilities.ByteArray;

public class ObjectEffectDate extends ObjectEffect {
	public int year = 0;
	public int month = 0;
	public int day = 0;
	public int hour = 0;
	public int minute = 0;

	public ObjectEffectDate(ByteArray buffer) {
		super(buffer);
		this.year = buffer.readVarShort();
		this.month = buffer.readByte();
		this.day = buffer.readByte();
		this.hour = buffer.readByte();
		this.minute = buffer.readByte();
	}
}