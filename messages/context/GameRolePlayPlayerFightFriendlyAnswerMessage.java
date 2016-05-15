package messages.context;

import messages.Message;

public class GameRolePlayPlayerFightFriendlyAnswerMessage extends Message {
	public int fightId = 0;
	public boolean accept = false;

	@Override
	public void serialize() {
		this.content.writeInt(this.fightId);
		this.content.writeBoolean(this.accept);
	}

	@Override
	public void deserialize() {
		
	}
}