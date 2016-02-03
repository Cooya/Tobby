package roleplay.currentmap;

import java.util.Vector;

import utilities.ByteArray;

public class HumanInformations {
    public ActorRestrictionsInformations restrictions;
    public boolean sex = false;
    public Vector<HumanOption> options;

	public HumanInformations(ByteArray buffer) {
        this.options = new Vector<HumanOption>();
        
        this.restrictions = new ActorRestrictionsInformations(buffer);
        this.sex = buffer.readBoolean();
        int protocolId;
        int nb = buffer.readShort();
        for(int i = 0; i < nb; ++i) {
        	protocolId = buffer.readShort();
        	if(protocolId == 406)
        		this.options.add(new HumanOption(buffer));
        	else if(protocolId == 407)
        		this.options.add(new HumanOptionEmote(buffer));
        	else if(protocolId == 408)
        		this.options.add(new HumanOptionTitle(buffer));
        	else if(protocolId == 409)
        		this.options.add(new HumanOptionGuild(buffer));
        	else if(protocolId == 410)
        		this.options.add(new HumanOptionFollowers(buffer));
        	else if(protocolId == 411)
        		this.options.add(new HumanOptionOrnament(buffer));
        	else if(protocolId == 425)
        		this.options.add(new HumanOptionAlliance(buffer));
        	else if(protocolId == 495)
        		this.options.add(new HumanOptionSkillUse(buffer));
        	else
        		throw new Error("Invalid or unhandled protocol id : " + protocolId + ".");
        }
	}
}