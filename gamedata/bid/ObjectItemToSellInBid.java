package gamedata.bid;

import utilities.ByteArray;

public class ObjectItemToSellInBid extends ObjectItemToSell {
	public int unsoldDelay = 0;

	public ObjectItemToSellInBid(ByteArray buffer) {
		super(buffer);
		this.unsoldDelay = buffer.readInt();
	}
}