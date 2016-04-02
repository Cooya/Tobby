package messages.connection;

import utilities.ByteArray;

public class CharacterFirstSelectionMessage extends CharacterSelectionMessage {
	public boolean doTutorial = false;
	
	public CharacterFirstSelectionMessage() {
		super();
	}
	
	public void serialize() {
		ByteArray buffer = new ByteArray();
		super.serialize(buffer);
		buffer.writeBoolean(this.doTutorial);
		super.completeInfos(buffer);
	}
}