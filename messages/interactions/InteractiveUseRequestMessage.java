package messages.interactions;

import messages.Message;

public class InteractiveUseRequestMessage extends Message {
	public int elemId = 0;
	public int skillInstanceUid = 0;
	
	@Override
	public void serialize() {
		this.content.writeVarInt(this.elemId);
		this.content.writeVarInt(this.skillInstanceUid);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}