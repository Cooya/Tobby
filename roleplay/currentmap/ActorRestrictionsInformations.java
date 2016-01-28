package roleplay.currentmap;

import utilities.BooleanByteWrapper;
import utilities.ByteArray;

public class ActorRestrictionsInformations {
    public boolean cantBeAggressed = false;
    public boolean cantBeChallenged = false;
    public boolean cantTrade = false;
    public boolean cantBeAttackedByMutant = false;
    public boolean cantRun = false;
    public boolean forceSlowWalk = false;
    public boolean cantMinimize = false;
    public boolean cantMove = false;
    public boolean cantAggress = false;
    public boolean cantChallenge = false;
    public boolean cantExchange = false;
    public boolean cantAttack = false;
    public boolean cantChat = false;
    public boolean cantBeMerchant = false;
    public boolean cantUseObject = false;
    public boolean cantUseTaxCollector = false;
    public boolean cantUseInteractive = false;
    public boolean cantSpeakToNPC = false;
    public boolean cantChangeZone = false;
    public boolean cantAttackMonster = false;
    public boolean cantWalk8Directions = false;
	
	public ActorRestrictionsInformations(ByteArray buffer) {
		int nb = buffer.readByte();
        this.cantBeAggressed = BooleanByteWrapper.getFlag(nb, 0);
        this.cantBeChallenged = BooleanByteWrapper.getFlag(nb, 1);
        this.cantTrade = BooleanByteWrapper.getFlag(nb, 2);
        this.cantBeAttackedByMutant = BooleanByteWrapper.getFlag(nb, 3);
        this.cantRun = BooleanByteWrapper.getFlag(nb, 4);
        this.forceSlowWalk = BooleanByteWrapper.getFlag(nb, 5);
        this.cantMinimize = BooleanByteWrapper.getFlag(nb, 6);
        this.cantMove = BooleanByteWrapper.getFlag(nb, 7);
        
        nb = buffer.readByte();
        this.cantAggress = BooleanByteWrapper.getFlag(nb, 0);
        this.cantChallenge = BooleanByteWrapper.getFlag(nb, 1);
        this.cantExchange = BooleanByteWrapper.getFlag(nb, 2);
        this.cantAttack = BooleanByteWrapper.getFlag(nb, 3);
        this.cantChat = BooleanByteWrapper.getFlag(nb, 4);
        this.cantBeMerchant = BooleanByteWrapper.getFlag(nb, 5);
        this.cantUseObject = BooleanByteWrapper.getFlag(nb, 6);
        this.cantUseTaxCollector = BooleanByteWrapper.getFlag(nb, 7);
        
        nb = buffer.readByte();
        this.cantUseInteractive = BooleanByteWrapper.getFlag(nb, 0);
        this.cantSpeakToNPC = BooleanByteWrapper.getFlag(nb, 1);
        this.cantChangeZone = BooleanByteWrapper.getFlag(nb, 2);
        this.cantAttackMonster = BooleanByteWrapper.getFlag(nb, 3);
        this.cantWalk8Directions = BooleanByteWrapper.getFlag(nb, 4);
	}
}
