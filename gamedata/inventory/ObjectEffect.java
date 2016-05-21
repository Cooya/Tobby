package gamedata.inventory;

import utilities.ByteArray;

public class ObjectEffect {
	public int actionId = 0;
	
	public ObjectEffect(ByteArray buffer) {
		this.actionId = buffer.readVarShort();
	}
}