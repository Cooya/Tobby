package messages.interactions;

import main.Emulation;
import messages.Message;
import utilities.ByteArray;

public class NpcGenericActionRequestMessage extends Message {
	public int npcId = 0;
	public int npcActionId = 0;
	public int npcMapId = 0;

	public NpcGenericActionRequestMessage() {
		super();
	}
	
	public void serialize(int npcId, int npcActionId, int npcMapId, int instanceId) {
		this.npcId = npcId;
		this.npcActionId = npcActionId;
		this.npcMapId = npcMapId;
		
		ByteArray buffer = new ByteArray();
		buffer.writeInt(this.npcId);
		buffer.writeByte(this.npcActionId);
		buffer.writeInt(this.npcMapId);
		this.completeInfos(Emulation.hashMessage(buffer, instanceId));
	}
}