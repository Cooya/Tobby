package messages.exchanges;

import gamedata.bid.ObjectItemToSellInBid;
import messages.NetworkMessage;

public class ExchangeBidHouseItemAddOkMessage extends NetworkMessage {
	public ObjectItemToSellInBid itemInfo;

	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		this.itemInfo = new ObjectItemToSellInBid(this.content);
	}
}