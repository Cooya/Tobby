package messages.context;

import java.util.Vector;

import messages.NetworkMessage;

public class GameMapMovementRequestMessage extends NetworkMessage {
	public Vector<Integer> keyMovements;
	public int mapId = 0;

	@Override
	public void serialize() {
		this.content.writeShort(this.keyMovements.size());
		for(int i : this.keyMovements)
			this.content.writeShort(i);
		this.content.writeInt(mapId);
	}

	@Override
	public void deserialize() {
		// not implemented yet
	}
}