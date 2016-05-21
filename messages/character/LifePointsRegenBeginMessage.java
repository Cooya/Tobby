package messages.character;

import messages.NetworkMessage;

public class LifePointsRegenBeginMessage extends NetworkMessage {
	public int regenRate = 0;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		this.regenRate = this.content.readByte();
	}
}