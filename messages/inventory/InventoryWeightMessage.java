package messages.inventory;

import messages.NetworkMessage;

public class InventoryWeightMessage extends NetworkMessage {
	public int weight = 0;
	public int weightMax = 0;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		this.weight = this.content.readVarInt();
		this.weightMax = this.content.readVarInt();
	}
}