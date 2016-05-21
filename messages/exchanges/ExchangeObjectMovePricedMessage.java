package messages.exchanges;

public class ExchangeObjectMovePricedMessage extends ExchangeObjectMoveMessage {
	public int price = 0;
	
	@Override
	public void serialize() {
		super.serialize();
		this.content.writeVarInt(this.price);
	}
	
	@Override
	public void deserialize() {
		
	}
}