package messages.connection;

import gamedata.ProtocolTypeManager;
import gamedata.character.CharacterBaseInformations;

import java.util.Vector;

import messages.NetworkMessage;

public class BasicCharactersListMessage extends NetworkMessage {
	public Vector<CharacterBaseInformations> characters;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		this.characters = new Vector<CharacterBaseInformations>();
		int nb = this.content.readShort();
		for(int i = 0; i < nb; ++i)
			this.characters.add((CharacterBaseInformations) ProtocolTypeManager.getInstance(this.content.readShort(), this.content));
	}
}