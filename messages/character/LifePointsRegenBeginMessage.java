package messages.character;

import messages.Message;

public class LifePointsRegenBeginMessage extends Message {
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