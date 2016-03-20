package messages.fights;

import utilities.ByteArray;
import messages.Message;

public class GameActionFightCastOnTargetRequestMessage extends Message {
	public int spellId = 0;
	public double targetId = 0;
	
	public GameActionFightCastOnTargetRequestMessage() {
		super();
	}
	
	public void serialize() {
		ByteArray buffer = new ByteArray();
		buffer.writeVarShort(this.spellId);
		buffer.writeDouble(this.targetId);
		super.completeInfos(buffer);
	}
}