package messages.fights;

import gamedata.ProtocolTypeManager;
import gamedata.fight.GameFightFighterInformations;
import messages.Message;
import utilities.ByteArray;

public class GameFightShowFighterMessage extends Message {
	public GameFightFighterInformations informations;

	public GameFightShowFighterMessage(Message msg) {
		super(msg);
		deserialize();
	}

	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.informations = (GameFightFighterInformations) ProtocolTypeManager.getInstance(buffer.readShort(), buffer);
	}
}