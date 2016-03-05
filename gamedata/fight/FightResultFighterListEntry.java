package gamedata.fight;

import utilities.ByteArray;

public class FightResultFighterListEntry extends FightResultListEntry {
	public double id = 0;
	public boolean alive = false;
	
	public FightResultFighterListEntry(ByteArray buffer) {
		super(buffer);
		this.id = buffer.readDouble();
		this.alive = buffer.readBoolean();
	}
}