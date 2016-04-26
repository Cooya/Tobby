package messages.fights;

import gamedata.ProtocolTypeManager;
import gamedata.fight.GameFightFighterInformations;
import messages.Message;

public class GameFightShowFighterMessage extends Message {
	public GameFightFighterInformations informations;

	@Override
	public void serialize() {
		// not implemented yet
	}

	@Override
	public void deserialize() {
		this.informations = (GameFightFighterInformations) ProtocolTypeManager.getInstance(this.content.readShort(), this.content);
	}
}