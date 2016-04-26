package messages.exchanges;

public class ExchangePlayerRequestMessage extends ExchangeRequestMessage {
	public double target;
	
	@Override
	public void serialize() {
		super.serialize();
		this.content.writeVarLong(this.target);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}