package messages.maps;

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
        for(int i = 0; i < nb; ++i) {
        	buffer.readShort(); // id du message HumanOption
        	this.options.add(new HumanOption(buffer));
        }
	}
}