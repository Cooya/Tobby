package messages.fights;

import messages.NetworkMessage;

public class GameActionFightCastRequestMessage extends NetworkMessage {
	public int spellId = 0;
	public short cellId = 0;

	@Override
	public void serialize() {
		this.content.writeVarShort(spellId);
		this.content.writeShort(cellId);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}