package gamedata.currentmap;

import gamedata.ProtocolTypeManager;

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
        int nb = buffer.readShort();
        for(int i = 0; i < nb; ++i)
        	this.options.add((HumanOption) ProtocolTypeManager.getInstance(buffer.readShort(), buffer));
	}
}