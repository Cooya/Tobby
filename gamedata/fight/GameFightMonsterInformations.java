package gamedata.fight;

import utilities.ByteArray;

public class GameFightMonsterInformations extends GameFightAIInformations {
	public int creatureGenericId = 0;
	public int creatureGrade = 0;

	public GameFightMonsterInformations(ByteArray buffer) {
		super(buffer);
		this.creatureGenericId = buffer.readVarShort();
		this.creatureGrade = buffer.readByte();
	}
}