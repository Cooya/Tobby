package gamedata.fight;

import gamedata.currentmap.EntityDispositionInformations;
import utilities.ByteArray;

public class FightEntityDispositionInformations extends EntityDispositionInformations {
	public double carryingCharacterId = 0;

	public FightEntityDispositionInformations(ByteArray buffer) {
		super(buffer);
		this.carryingCharacterId = buffer.readDouble();
	}
}