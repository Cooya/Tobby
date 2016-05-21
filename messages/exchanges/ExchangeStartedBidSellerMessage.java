package messages.exchanges;

import gamedata.bid.ObjectItemToSellInBid;
import gamedata.bid.SellerBuyerDescriptor;

import java.util.Vector;

import messages.NetworkMessage;

public class ExchangeStartedBidSellerMessage extends NetworkMessage {
	public SellerBuyerDescriptor sellerDescriptor;
	public Vector<ObjectItemToSellInBid> objectsInfos;
	
	@Override
	public void serialize() {

	}
	@Override
	public void deserialize() {
		this.sellerDescriptor = new SellerBuyerDescriptor(this.content);
		int nb = this.content.readShort();
		this.objectsInfos = new Vector<ObjectItemToSellInBid>(nb);
		for(int i = 0; i < nb; ++i)
			this.objectsInfos.add(new ObjectItemToSellInBid(this.content));
	}
}