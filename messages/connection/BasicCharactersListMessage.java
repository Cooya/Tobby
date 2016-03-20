package messages.connection;

import gamedata.ProtocolTypeManager;
import gamedata.character.CharacterBaseInformations;

import java.util.Vector;

import utilities.ByteArray;
import messages.Message;

public class BasicCharactersListMessage extends Message {
	public Vector<CharacterBaseInformations> characters;

	public BasicCharactersListMessage(Message msg) {
		super(msg);
		this.characters = new Vector<CharacterBaseInformations>();
	}
	
	protected void deserialize(ByteArray buffer) {
		int nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			this.characters.add((CharacterBaseInformations) ProtocolTypeManager.getInstance(buffer.readShort(), buffer));
	}
	
	public void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		deserialize(buffer);
	}
}