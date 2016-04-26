package messages.context;

import messages.Message;

public class GameRolePlayAttackMonsterRequestMessage extends Message {
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