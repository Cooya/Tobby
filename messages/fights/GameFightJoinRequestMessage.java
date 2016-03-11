package messages.fights;

import messages.Message;
import utilities.ByteArray;

public class GameFightJoinRequestMessage extends Message {
	public double fighterId = 0;
	public int fightId = 0;
	
	public GameFightJoinRequestMessage() {
		super();
	}
	
	public void serialize() {
		ByteArray buffer = new ByteArray();
		buffer.writeDouble(this.fighterId);
		buffer.writeInt(this.fightId);
		super.completeInfos(buffer);
	}
}