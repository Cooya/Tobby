package messages.context;

import java.util.Vector;

import messages.Message;
import utilities.ByteArray;

public class GameMapMovementMessage extends Message{
	public Vector<Integer> keyMovements;
	public double actorId = 0;

	public GameMapMovementMessage(Message msg) {
		super(msg);
		this.keyMovements = new Vector<Integer>();
		deserialize();
	}

	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		int nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			this.keyMovements.add(buffer.readShort());
		this.actorId = buffer.readDouble();
	}
}
