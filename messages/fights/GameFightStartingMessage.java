package messages.fights;

import messages.NetworkMessage;

public class GameFightStartingMessage extends NetworkMessage {
	public int fightType = 0;
	public double attackerId = 0;
	public double defenderId = 0;

	@Override
	public void serialize() {
		// not implemented yet
	}

	@Override
	public void deserialize() {
		this.fightType = this.content.readByte();
		this.attackerId = this.content.readDouble();
		this.defenderId = this.content.readDouble();
	}
}