package messages.fights;

import gamedata.ProtocolTypeManager;
import gamedata.fight.GameFightFighterInformations;

import java.util.Vector;

import messages.NetworkMessage;

public class GameFightSynchronizeMessage extends NetworkMessage {
	public Vector<GameFightFighterInformations> fighters;

	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		this.fighters = new Vector<GameFightFighterInformations>();
		int nb = this.content.readShort();
		for(int i = 0; i < nb; ++i)
			this.fighters.add((GameFightFighterInformations) ProtocolTypeManager.getInstance(this.content.readShort(), this.content));
	}
}