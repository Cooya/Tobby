package messages.exchanges;

public class ExchangeRequestedTradeMessage extends ExchangeRequestedMessage {
	public double source = 0;
	public double target = 0;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		super.deserialize();
		this.source = this.content.readVarLong();
		this.target = this.content.readVarLong();
	}
}