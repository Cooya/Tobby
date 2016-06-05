package messages.fights;

import gamedata.ProtocolTypeManager;
import gamedata.fight.GameFightFighterInformations;

import messages.NetworkMessage;

public class GameFightSynchronizeMessage extends NetworkMessage {
	public GameFightFighterInformations[] fighters;

	@Override
	public void serialize() {
		
	}
	
	@Override
	public void deserialize() {
		int nb = this.content.readShort();
		this.fighters = new GameFightFighterInformations[nb];
		for(int i = 0; i < nb; ++i)
			this.fighters[i] = (GameFightFighterInformations) ProtocolTypeManager.getInstance(this.content.readShort(), this.content);
	}
}