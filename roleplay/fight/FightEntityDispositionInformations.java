package roleplay.fight;

import roleplay.currentmap.EntityDispositionInformations;
import utilities.ByteArray;

public class FightEntityDispositionInformations extends EntityDispositionInformations{

	public static final int protocolId = 217;
    
    public double carryingCharacterId = 0;
	
	public FightEntityDispositionInformations(ByteArray buffer) {
		super(buffer);
        this.carryingCharacterId = buffer.readDouble();
        if(this.carryingCharacterId < -9.007199254740992E15 || this.carryingCharacterId > 9.007199254740992E15)
        {
           throw new Error("Forbidden value (" + this.carryingCharacterId + ") on element of FightEntityDispositionInformations.carryingCharacterId.");
        }
	}

}
