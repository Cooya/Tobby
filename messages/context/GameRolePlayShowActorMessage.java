package messages.context;

import gamedata.ProtocolTypeManager;
import gamedata.context.GameRolePlayActorInformations;
import messages.Message;

public class GameRolePlayShowActorMessage extends Message {
	public GameRolePlayActorInformations informations;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		this.informations = (GameRolePlayActorInformations) ProtocolTypeManager.getInstance(this.content.readShort(), this.content);
	}
}