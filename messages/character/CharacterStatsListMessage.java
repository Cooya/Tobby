package messages.character;

import gamedata.character.CharacterCharacteristicsInformations;
import utilities.ByteArray;
import messages.Message;

public class CharacterStatsListMessage extends Message {
	public CharacterCharacteristicsInformations stats;
	
	public CharacterStatsListMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.stats = new CharacterCharacteristicsInformations(buffer);
	}
}