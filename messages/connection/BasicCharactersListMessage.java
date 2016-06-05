package messages.connection;

import gamedata.ProtocolTypeManager;
import gamedata.character.CharacterBaseInformations;

import messages.NetworkMessage;

public class BasicCharactersListMessage extends NetworkMessage {
	public CharacterBaseInformations[] characters;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		int nb = this.content.readShort();
		this.characters = new CharacterBaseInformations[nb];
		for(int i = 0; i < nb; ++i)
			this.characters[i] = (CharacterBaseInformations) ProtocolTypeManager.getInstance(this.content.readShort(), this.content);
	}
}