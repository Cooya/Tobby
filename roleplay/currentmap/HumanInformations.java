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
        	else
        		throw new Error("Invalid or unhandled protocol id : " + protocolId + ".");
        }
	}
}