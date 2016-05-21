package messages.exchanges;

import messages.NetworkMessage;

public class ExchangeBidHouseItemRemoveOkMessage extends NetworkMessage {
	public int sellerId = 0;

	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		this.sellerId = this.content.readInt();
	}
}