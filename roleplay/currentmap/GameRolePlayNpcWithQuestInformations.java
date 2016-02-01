package roleplay.currentmap;

import utilities.ByteArray;

public class GameRolePlayNpcWithQuestInformations extends GameRolePlayNpcInformations {
	public GameRolePlayNpcQuestFlag questFlag;
	
	public GameRolePlayNpcWithQuestInformations(ByteArray buffer) {
		super(buffer);
		this.questFlag = new GameRolePlayNpcQuestFlag(buffer);
	}
}