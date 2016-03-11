package gamedata.context;

import utilities.ByteArray;

public class HumanOptionSkillUse extends HumanOption {
	public int elementId = 0;
    public int skillId = 0;
    public double skillEndTime = 0;

    public HumanOptionSkillUse(ByteArray buffer) {
		super(buffer);
		this.elementId = buffer.readVarInt();
		this.skillId = buffer.readVarShort();
		this.skillEndTime = buffer.readDouble();
	}
}
