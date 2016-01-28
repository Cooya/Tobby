package roleplay.currentmap;

import utilities.ByteArray;

public class InteractiveElementSkill {
    public int skillId = 0;
    public int skillInstanceUid = 0;
	
	public InteractiveElementSkill(ByteArray buffer) {
        this.skillId = buffer.readVarInt();
        this.skillInstanceUid = buffer.readInt();
	}
}
