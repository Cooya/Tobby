package roleplay.currentmap;

import utilities.ByteArray;

public class GameContextActorInformations {
    public double contextualId = 0;
    public EntityLook look;
    public EntityDispositionInformations disposition;

	public GameContextActorInformations(ByteArray buffer) {
        this.contextualId = buffer.readDouble();
        this.look = new EntityLook(buffer);
        int protocolId = buffer.readShort();
        if(protocolId == 60)
        	this.disposition = new EntityDispositionInformations(buffer);
        else
        	throw new Error("Invalid or unhandled protocol id : " + protocolId + ".");
	}
}
