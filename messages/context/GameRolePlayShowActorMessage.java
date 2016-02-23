package messages.context;

import gamedata.ProtocolTypeManager;
import gamedata.currentmap.GameRolePlayActorInformations;
import messages.Message;
import utilities.ByteArray;

public class GameRolePlayShowActorMessage extends Message {
	public GameRolePlayActorInformations informations;
	
	public GameRolePlayShowActorMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		informations = (GameRolePlayActorInformations) ProtocolTypeManager.getInstance(buffer.readShort(), buffer);
	}
}