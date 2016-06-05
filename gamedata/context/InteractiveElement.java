package gamedata.context;

import gamedata.ProtocolTypeManager;

import utilities.ByteArray;

public class InteractiveElement {
    public int elementId = 0;
    public int elementTypeId = 0;
    public InteractiveElementSkill[] enabledSkills;
    public InteractiveElementSkill[] disabledSkills;

	public InteractiveElement(ByteArray buffer) {
        this.elementId = buffer.readInt();
        this.elementTypeId = buffer.readInt();
        int nb = buffer.readShort();
        this.enabledSkills = new InteractiveElementSkill[nb];
        for(int i = 0; i < nb; ++i)
        	this.enabledSkills[i] = (InteractiveElementSkill) ProtocolTypeManager.getInstance(buffer.readShort(), buffer);
        nb = buffer.readShort();
        this.disabledSkills = new InteractiveElementSkill[nb];
        for(int i = 0; i < nb; ++i)
        	this.disabledSkills[i] = (InteractiveElementSkill) ProtocolTypeManager.getInstance(buffer.readShort(), buffer);
	}
}