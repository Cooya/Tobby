package messages.maps;

import utilities.ByteArray;

public class GameContextActorInformations {
    public double contextualId = 0;
    public EntityLook look;
    public EntityDispositionInformations disposition;

	public GameContextActorInformations(ByteArray buffer) {
        this.contextualId = buffer.readDouble();
        this.look = new EntityLook(buffer);
        if(buffer.readShort() != 60)
        	throw new Error("Invalid value read");
        this.disposition = new EntityDispositionInformations(buffer);
	}
}
