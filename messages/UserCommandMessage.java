package messages;

public class UserCommandMessage extends NetworkMessage {
	public String command;

	@Override
	public void serialize() {
		this.content.writeUTF(this.command);
	}

	@Override
	public void deserialize() {
		this.command = this.content.readUTF();
	}
}