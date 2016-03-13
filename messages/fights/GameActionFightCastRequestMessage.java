package messages.fights;

import main.Emulation;
import messages.Message;
import utilities.ByteArray;

public class GameActionFightCastRequestMessage extends Message {
	public int spellId = 0;
	public short cellId = 0;

	public GameActionFightCastRequestMessage() {
		super();
	}

	public void serialize(int spellId, short cellId, int instanceId) {
		ByteArray buffer = new ByteArray();
		buffer.writeVarShort(spellId);
		buffer.writeShort(cellId);
		completeInfos(Emulation.hashMessage(buffer, instanceId));
	}
}