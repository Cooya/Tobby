package messages.fights;

import utilities.ByteArray;
import messages.Message;

public class GameFightOptionStateUpdateMessage extends Message {
    public int fightId = 0;
    public int teamId = 2;
    public int option = 3;
    public boolean state = false;
	
	public GameFightOptionStateUpdateMessage(Message msg) {
		super(msg);
		deserialize();
	}

	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.fightId = buffer.readShort();
		this.teamId = buffer.readByte();
		this.option = buffer.readByte();
		this.state = buffer.readBoolean();
	}
}