package messages.character;

import messages.Message;
import utilities.ByteArray;

public class InventoryWeightMessage extends Message {
	public int weight = 0;
	public int weightMax = 0;

	public InventoryWeightMessage(Message msg) {
		super(msg);
		deserialize();
	}

	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.weight = buffer.readVarInt();
		this.weightMax = buffer.readVarInt();
	}
}