package roleplay.currentmap;

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
        int protocolId;
        int nb = buffer.readShort();
        for(int i = 0; i < nb; ++i) {
        	protocolId = buffer.readShort();
        	if(protocolId == 219)
        		this.enabledSkills.add(new InteractiveElementSkill(buffer));
        	else
        		throw new Error("Invalid or unhandled protocol id : " + protocolId + ".");
        }
        nb = buffer.readShort();
        for(int i = 0; i < nb; ++i) {
        	protocolId = buffer.readShort();
        	if(protocolId == 219)
        		this.disabledSkills.add(new InteractiveElementSkill(buffer));
        	else
        		throw new Error("Invalid or unhandled protocol id : " + protocolId + ".");
        }
	}
}