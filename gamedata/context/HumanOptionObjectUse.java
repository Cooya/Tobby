package gamedata.context;

import utilities.ByteArray;

public class HumanOptionObjectUse extends HumanOption {
	public int delayTypeId = 0;
	public double delayEndTime = 0;
	public int objectGID = 0;

	public HumanOptionObjectUse(ByteArray buffer) {
		super(buffer);
        this.delayTypeId = buffer.readByte();
        this.delayEndTime = buffer.readDouble();
        this.objectGID = buffer.readVarShort();
	}
}