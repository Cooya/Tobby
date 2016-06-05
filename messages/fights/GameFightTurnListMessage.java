package messages.fights;

import messages.NetworkMessage;

public class GameFightTurnListMessage extends NetworkMessage {
	public double[] ids;
	public double[] deadsIds;

	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		int nb = this.content.readShort();
		this.ids = new double[nb];
		for(int i = 0; i < nb; ++i)
			this.ids[i] = this.content.readDouble();
		nb = this.content.readShort();
		this.deadsIds = new double[nb];
		for(int i = 0; i < nb; ++i)
			this.deadsIds[i] = this.content.readDouble();
	}
}