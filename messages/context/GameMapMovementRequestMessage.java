package messages.context;

import messages.NetworkMessage;

public class GameMapMovementRequestMessage extends NetworkMessage {
	public int[] keyMovements;
	public int mapId = 0;

	@Override
	public void serialize() {
		this.content.writeShort(this.keyMovements.length);
		for(int i : this.keyMovements)
			this.content.writeShort(i);
		this.content.writeInt(mapId);
	}

	@Override
	public void deserialize() {
		
	}
}