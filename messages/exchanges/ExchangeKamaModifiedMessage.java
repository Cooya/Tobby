package messages.exchanges;

public class ExchangeKamaModifiedMessage extends ExchangeObjectMessage {
	public int quantity;

	@Override
	public void serialize() {
		
	}
	
	@Override
	public void deserialize() {
		super.deserialize();
		this.quantity = this.content.readVarInt();
	}
}