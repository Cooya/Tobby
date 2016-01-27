package messages.connection;

import messages.Message;
import utilities.ByteArray;
import utilities.Int64;

public class CharactersListMessage extends Message {
	private int nbCharacters;
	private Int64 id;
	
	public CharactersListMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.nbCharacters = buffer.readShort();
		if(this.nbCharacters > 1)
			throw new Error("Too many characters on this server.");
		buffer.readShort(); // short inutile
		this.id = buffer.readVarLong();
	}
	
	public Int64 getCharacterId() {
		return this.id;
	}
}
