package messages.connection;

import messages.Message;
import utilities.ByteArray;
import utilities.Int64;

// gros raccourci sur ce fichier, il n'a pas été traduit à la lettre pour gain de temps
public class CharactersListMessage extends Message {
	public int nbCharacters;
	public Int64 id;
	
	public CharactersListMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.nbCharacters = buffer.readShort();
		buffer.readShort(); // short inutile
		this.id = buffer.readVarLong();
	}
}