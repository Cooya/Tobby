package messages.fights;

import messages.NetworkMessage;

public class GameActionFightCastOnTargetRequestMessage extends NetworkMessage {
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