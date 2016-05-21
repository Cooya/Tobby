package messages.context;

import java.util.Vector;

import messages.NetworkMessage;

public class GameMapMovementMessage extends NetworkMessage{
	public Vector<Integer> keyMovements;
	public double actorId = 0;
	
	@Override
	public void serialize() {
		// not implemented yet
	}

	@Override
	public void deserialize() {
		this.keyMovements = new Vector<Integer>();
		int nb = this.content.readShort();
		for(int i = 0; i < nb; ++i)
			this.keyMovements.add(this.content.readShort());
		this.actorId = this.content.readDouble();
	}
}