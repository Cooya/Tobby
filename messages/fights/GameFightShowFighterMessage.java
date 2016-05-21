package messages.fights;

import gamedata.ProtocolTypeManager;
import gamedata.fight.GameFightFighterInformations;
import messages.NetworkMessage;

public class GameFightShowFighterMessage extends NetworkMessage {
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