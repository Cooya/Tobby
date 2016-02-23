package messages.interactions;

import utilities.ByteArray;
import main.Emulation;
import messages.Message;

public class InteractiveUseRequestMessage extends Message {
	public int elemId = 0;
	public int skillInstanceUid = 0;
	
	public InteractiveUseRequestMessage() {
		super();
	}
	
	public void serialize(int elemId, int skillInstanceUid, int instanceId) {
		this.elemId = elemId;
		this.skillInstanceUid = skillInstanceUid;
		
		ByteArray buffer = new ByteArray();
		buffer.writeVarInt(this.elemId);
		buffer.writeVarInt(this.skillInstanceUid);
		completeInfos(Emulation.hashMessage(buffer, instanceId));
	}
}