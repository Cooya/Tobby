package messages.exchanges;

import gamedata.bid.ObjectItemToSellInBid;
import gamedata.bid.SellerBuyerDescriptor;

import messages.NetworkMessage;

public class ExchangeStartedBidSellerMessage extends NetworkMessage {
	public SellerBuyerDescriptor sellerDescriptor;
	public ObjectItemToSellInBid[] objectsInfos;
	
	@Override
	public void serialize() {

	}
	@Override
	public void deserialize() {
		this.sellerDescriptor = new SellerBuyerDescriptor(this.content);
		int nb = this.content.readShort();
		this.objectsInfos = new ObjectItemToSellInBid[nb];
		for(int i = 0; i < nb; ++i)
			this.objectsInfos[i] = new ObjectItemToSellInBid(this.content);
	}
}