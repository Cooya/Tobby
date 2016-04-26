package messages.connection;

public class CharactersListMessage extends BasicCharactersListMessage {
	public boolean hasStartupActions = false;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		super.deserialize();
		this.hasStartupActions = this.content.readBoolean();
	}
}