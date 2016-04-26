package messages.character;

import messages.Message;

public class StatsUpgradeRequestMessage extends Message {
	public boolean useAdditionnal = false;
	public int statId = 11;
	public int boostPoint = 0;

	@Override
	public void serialize() {
		this.content.writeBoolean(this.useAdditionnal);
		this.content.writeByte(this.statId);
		this.content.writeVarShort(this.boostPoint);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}