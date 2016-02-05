package messages.context;

import utilities.ByteArray;
import messages.Message;

public class MapFightCountMessage extends Message {
	public int fightCount = 0;
	
	public MapFightCountMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.fightCount = buffer.readVarShort();
	}
}