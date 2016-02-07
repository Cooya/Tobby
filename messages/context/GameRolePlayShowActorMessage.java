package messages.context;

import messages.Message;
import roleplay.ProtocolTypeManager;
import roleplay.currentmap.GameRolePlayActorInformations;
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