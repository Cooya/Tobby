package messages.context;

import utilities.ByteArray;
import messages.Message;

public class InteractiveUsedMessage extends Message {
	public double entityId = 0;
	public int elemId = 0;
	public int skillId = 0;
	public int duration = 0;
	
	public InteractiveUsedMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.entityId = buffer.readVarLong().toNumber();
		this.elemId = buffer.readVarInt();
		this.skillId = buffer.readVarShort();
		this.duration = buffer.readVarShort();
	}
}