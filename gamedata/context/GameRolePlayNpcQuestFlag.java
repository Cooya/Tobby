package gamedata.context;

import utilities.ByteArray;

public class GameRolePlayNpcQuestFlag {
	public int[] questsToValidId;
	public int[] questsToStartId;

	public GameRolePlayNpcQuestFlag(ByteArray buffer) {
		int nb = buffer.readShort();
		this.questsToValidId = new int[nb];
		for(int i = 0; i < nb; ++i)
			this.questsToValidId[i] = buffer.readVarShort();
		nb = buffer.readShort();
		this.questsToStartId = new int[nb];
		for(int i = 0; i < nb; ++i)
			this.questsToStartId[i] = buffer.readVarShort();
	}
}