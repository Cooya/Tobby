package messages.exchanges;

public class ExchangeLeaveMessage extends LeaveDialogMessage {
	public boolean success = false;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		super.deserialize();
		this.success = this.content.readBoolean();
	}
}