package messages.connection;

public class IdentificationFailedBannedMessage extends IdentificationFailedMessage {
	public double banEndDate = 0;

	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		super.deserialize();
		this.banEndDate = this.content.readDouble();
	}
}