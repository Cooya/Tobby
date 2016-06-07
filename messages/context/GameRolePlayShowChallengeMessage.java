package messages.context;

import gamedata.context.FightCommonInformations;
import messages.NetworkMessage;

public class GameRolePlayShowChallengeMessage extends NetworkMessage {
	public FightCommonInformations commonsInfos;

	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		this.commonsInfos = new FightCommonInformations(this.content);
	}
}