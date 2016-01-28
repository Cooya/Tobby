package roleplay.currentmap;

import utilities.ByteArray;

public class GameRolePlayCharacterInformations extends GameRolePlayHumanoidInformations {
	public ActorAlignmentInformations alignmentInfos;

	public GameRolePlayCharacterInformations(ByteArray buffer) {
		super(buffer);
		this.alignmentInfos = new ActorAlignmentInformations(buffer);
	}
}
