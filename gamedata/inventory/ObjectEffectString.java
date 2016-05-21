package gamedata.inventory;

import utilities.ByteArray;

public class ObjectEffectString extends ObjectEffect {
	public String value = "";

	public ObjectEffectString(ByteArray buffer) {
		super(buffer);
		this.value = buffer.readUTF();
	}
}