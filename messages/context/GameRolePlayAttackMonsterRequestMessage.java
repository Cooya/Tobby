package messages.context;

import messages.NetworkMessage;

public class GameRolePlayAttackMonsterRequestMessage extends NetworkMessage {
	public double monsterGroupId = 0;

	@Override
	public void serialize() {
		this.content.writeDouble(this.monsterGroupId);
	}

	@Override
	public void deserialize() {
		// not implemented yet
	}
}