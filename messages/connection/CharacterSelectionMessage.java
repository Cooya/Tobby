package messages.connection;

import messages.Message;
import utilities.ByteArray;

public class CharacterSelectionMessage extends Message {
	
	public CharacterSelectionMessage() {
		super();
	}
	
	public void serialize(CharactersListMessage CLM) {
		ByteArray buffer = new ByteArray();
		buffer.writeVarLong(CLM.id);
		
		completeInfos(buffer);
	}
}