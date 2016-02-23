package gamedata.currentmap;

import gamedata.ProtocolTypeManager;

import java.util.Vector;

import utilities.ByteArray;

public class InteractiveElement {
    public int elementId = 0;
    public int elementTypeId = 0;
    public Vector<InteractiveElementSkill> enabledSkills;
    public Vector<InteractiveElementSkill> disabledSkills;

	public InteractiveElement(ByteArray buffer) {
		this.enabledSkills = new Vector<InteractiveElementSkill>();
        this.disabledSkills = new Vector<InteractiveElementSkill>();
        
        this.elementId = buffer.readInt();
        this.elementTypeId = buffer.readInt();
        int nb = buffer.readShort();
        for(int i = 0; i < nb; ++i)
        	this.enabledSkills.add((InteractiveElementSkill) ProtocolTypeManager.getInstance(buffer.readShort(), buffer));
        nb = buffer.readShort();
        for(int i = 0; i < nb; ++i)
        	this.disabledSkills.add((InteractiveElementSkill) ProtocolTypeManager.getInstance(buffer.readShort(), buffer));
	}
}