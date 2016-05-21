package gamedata.inventory;

import utilities.ByteArray;

public class ObjectEffectMount extends ObjectEffect {
	public int mountId = 0;
	public double date = 0;
	public int modelId = 0;

	public ObjectEffectMount(ByteArray buffer) {
		super(buffer);
		this.mountId = buffer.readInt();
		this.date = buffer.readDouble();
		this.modelId = buffer.readVarShort();
	}
}