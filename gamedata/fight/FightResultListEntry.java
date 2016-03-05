package gamedata.fight;

import utilities.ByteArray;

public class FightResultListEntry {
    public int outcome = 0;
    public int wave = 0;
    public FightLoot rewards;
    
    public FightResultListEntry(ByteArray buffer) {
    	this.outcome = buffer.readVarShort();
    	this.wave = buffer.readByte();
    	this.rewards = new FightLoot(buffer);
    }
}