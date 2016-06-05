package messages.context;

import messages.NetworkMessage;

public class GameMapMovementMessage extends NetworkMessage{
	public int[] keyMovements;
	public double actorId = 0;
	
	@Override
	public void serialize() {
		// not implemented yet
	}

	@Override
	public void deserialize() {
		int nb = this.content.readShort();
		this.keyMovements = new int[nb];
		for(int i = 0; i < nb; ++i)
			this.keyMovements[i] = this.content.readShort();
		this.actorId = this.content.readDouble();
	}
}