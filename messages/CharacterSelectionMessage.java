package messages;

import utilities.ByteArray;

public class CharacterSelectionMessage extends Message {
	public static final int ID = 152;
	
	public CharacterSelectionMessage() {
		super(ID);
	}
	
	public void serialize(CharactersListMessage CLM) {
		ByteArray buffer = new ByteArray();
		buffer.writeVarLong(CLM.getCharacterId());
		
		this.size = buffer.getSize();
		this.lenofsize = computeLenOfSize(this.size);
		this.content = buffer.bytes();
	}
}
