package gamedata.currentmap;

import gamedata.ProtocolTypeManager;
import utilities.ByteArray;

public class GameContextActorInformations {
    public double contextualId = 0;
    public EntityLook look;
    public EntityDispositionInformations disposition;

	public GameContextActorInformations(ByteArray buffer) {
        this.contextualId = buffer.readDouble();
        this.look = new EntityLook(buffer);
        this.disposition = (EntityDispositionInformations) ProtocolTypeManager.getInstance(buffer.readShort(), buffer);
	}
}