package messages.character;

import gamedata.character.CharacterCharacteristicsInformations;
import messages.Message;

public class CharacterStatsListMessage extends Message {
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