package messages.fights;

import java.util.Vector;

import messages.Message;
import utilities.ByteArray;

public class GameFightTurnListMessage extends Message {
	public Vector<Double> ids;
	public Vector<Double> deadsIds;

	public GameFightTurnListMessage(Message msg) {
		super(msg);
		this.ids = new Vector<Double>();
		this.deadsIds = new Vector<Double>();
		deserialize();
	}

	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		int nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			this.ids.add(buffer.readDouble());
		nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			this.deadsIds.add(buffer.readDouble());
	}
}