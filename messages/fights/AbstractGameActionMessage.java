package messages.fights;

import messages.Message;

public class AbstractGameActionMessage extends Message {
	public int actionId = 0;
	public double sourceId = 0;
	
	@Override
	public void serialize() {
		// not implemented yet
	}

	@Override
	public void deserialize() {
		this.actionId = this.content.readVarShort();
		this.sourceId = this.content.readDouble();
	}
}