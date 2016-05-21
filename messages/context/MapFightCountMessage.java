package messages.context;

import messages.NetworkMessage;

public class MapFightCountMessage extends NetworkMessage {
	public int fightCount = 0;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		this.fightCount = this.content.readVarShort();
	}
}