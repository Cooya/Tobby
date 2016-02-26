package gamedata.fight;

import utilities.ByteArray;

public class GameFightMinimalStatsPreparation extends GameFightMinimalStats {
	public int initiative = 0;

	public GameFightMinimalStatsPreparation(ByteArray buffer) {
		super(buffer);
		this.initiative = buffer.readVarInt();
	}
}