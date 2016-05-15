package messages.context;

import messages.Message;

public class GameRolePlayPlayerFightFriendlyRequestedMessage extends Message {
	public int fightId = 0;
	public double sourceId = 0;
	public double targetId = 0;

	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		this.fightId = this.content.readInt();
        this.sourceId = this.content.readVarLong();
        this.targetId = this.content.readVarLong();
	}
}