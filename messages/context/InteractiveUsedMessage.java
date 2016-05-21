package messages.context;

import messages.NetworkMessage;

public class InteractiveUsedMessage extends NetworkMessage {
	public double entityId = 0;
	public int elemId = 0;
	public int skillId = 0;
	public int duration = 0;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		this.entityId = this.content.readVarLong();
		this.elemId = this.content.readVarInt();
		this.skillId = this.content.readVarShort();
		this.duration = this.content.readVarShort();
	}
}