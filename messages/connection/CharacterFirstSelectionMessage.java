package messages.connection;

public class CharacterFirstSelectionMessage extends CharacterSelectionMessage {
	public boolean doTutorial = false;
	
	@Override
	public void serialize() {
		super.serialize();
		this.content.writeBoolean(this.doTutorial);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}