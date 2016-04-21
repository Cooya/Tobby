package messages.fights;

import gamedata.ProtocolTypeManager;
import gamedata.fight.GameFightFighterInformations;

import java.util.Vector;

import messages.Message;
import utilities.ByteArray;

public class GameFightSynchronizeMessage extends Message {
	public Vector<GameFightFighterInformations> fighters;

	public GameFightSynchronizeMessage(Message msg) {
		super(msg);
		this.fighters = new Vector<GameFightFighterInformations>();
		deserialize();
	}

	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		int nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			this.fighters.add((GameFightFighterInformations) ProtocolTypeManager.getInstance(buffer.readShort(), buffer));
	}
}