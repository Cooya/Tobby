package messages.fights;

import java.util.Vector;

import messages.Message;

public class GameFightTurnListMessage extends Message {
	public Vector<Double> ids;
	public Vector<Double> deadsIds;

	@Override
	public void serialize() {
		// not implemented yet
	}

	@Override
	public void deserialize() {
		this.ids = new Vector<Double>();
		this.deadsIds = new Vector<Double>();
		int nb = this.content.readShort();
		for(int i = 0; i < nb; ++i)
			this.ids.add(this.content.readDouble());
		nb = this.content.readShort();
		for(int i = 0; i < nb; ++i)
			this.deadsIds.add(this.content.readDouble());
	}
}