package messages.context;

import messages.Message;

public class MapFightCountMessage extends Message {
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