package messages.context;

import messages.Message;
import roleplay.currentmap.GameRolePlayActorInformations;
import roleplay.currentmap.GameRolePlayCharacterInformations;
import roleplay.currentmap.GameRolePlayGroupMonsterInformations;
import roleplay.currentmap.GameRolePlayHumanoidInformations;
import roleplay.currentmap.GameRolePlayNamedActorInformations;
import roleplay.currentmap.GameRolePlayNpcInformations;
import roleplay.currentmap.GameRolePlayNpcWithQuestInformations;
import utilities.ByteArray;

public class GameRolePlayShowActorMessage extends Message {
	public GameRolePlayActorInformations informations;
	
	public GameRolePlayShowActorMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		int protocolId = buffer.readShort();
		switch(protocolId) {
			case 36 : informations = new GameRolePlayCharacterInformations(buffer); break;
			case 141 : informations = new GameRolePlayActorInformations(buffer); break;
			case 154 : informations = new GameRolePlayNamedActorInformations(buffer); break;
			case 159 : informations = new GameRolePlayHumanoidInformations(buffer); break;
			case 156 : informations = new GameRolePlayNpcInformations(buffer); break;
			case 160 : informations = new GameRolePlayGroupMonsterInformations(buffer); break;
			case 383 : informations = new GameRolePlayNpcWithQuestInformations(buffer); break;
			default : throw new Error("Invalid or unhandled protocol id : " + protocolId + ".");
		}
	}
}