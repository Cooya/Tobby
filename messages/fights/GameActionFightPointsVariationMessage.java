package messages.fights;

public class GameActionFightPointsVariationMessage extends AbstractGameActionMessage {
	public double targetId = 0;
	public int delta = 0;

	@Override
	public void serialize() {
		// not implemented yet
	}

	@Override
	public void deserialize() {
		super.deserialize();
		this.targetId = this.content.readDouble();
		this.delta = this.content.readShort();
	}
}