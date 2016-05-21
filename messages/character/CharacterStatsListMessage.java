package messages.character;

import gamedata.character.CharacterCharacteristicsInformations;
import messages.NetworkMessage;

public class CharacterStatsListMessage extends NetworkMessage {
	public CharacterCharacteristicsInformations stats;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		this.stats = new CharacterCharacteristicsInformations(this.content);
	}
}