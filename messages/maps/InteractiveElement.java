package messages.maps;

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
        for(int i = 0; i < nb; ++i) {
        	buffer.readShort(); // id du message InteractiveElementSkill
        	this.enabledSkills.add(new InteractiveElementSkill(buffer));
        }
        nb = buffer.readShort();
        for(int i = 0; i < nb; ++i) {
        	buffer.readShort(); // id du message InteractiveElementSkill
        	this.disabledSkills.add(new InteractiveElementSkill(buffer));
        }
	}
}
