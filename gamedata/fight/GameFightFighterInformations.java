package gamedata.fight;

import java.util.Vector;

import utilities.ByteArray;
import gamedata.ProtocolTypeManager;
import gamedata.context.GameContextActorInformations;

public class GameFightFighterInformations extends GameContextActorInformations {
	public int teamId = 2;
	public int wave = 0;
	public boolean alive = false;
	public GameFightMinimalStats stats;
	public Vector<Integer> previousPositions;

	public GameFightFighterInformations(ByteArray buffer) {
		super(buffer);
		this.previousPositions = new Vector<Integer>();
		this.teamId = buffer.readByte();
		this.wave = buffer.readByte();
		this.alive = buffer.readBoolean();
		this.stats = (GameFightMinimalStats) ProtocolTypeManager.getInstance(buffer.readShort(), buffer);
		int nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			this.previousPositions.add(buffer.readVarShort());
	}
}