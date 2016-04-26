package messages.fights;

import messages.Message;

public class GameActionFightCastOnTargetRequestMessage extends Message {
	public int spellId = 0;
	public double targetId = 0;
	
	@Override
	public void serialize() {
		this.content.writeVarShort(this.spellId);
		this.content.writeDouble(this.targetId);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}