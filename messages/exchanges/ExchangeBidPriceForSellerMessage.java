package messages.exchanges;

import java.util.Vector;

public class ExchangeBidPriceForSellerMessage extends ExchangeBidPriceMessage {
	public boolean allIdentical = false;
	public Vector<Integer> minimalPrices;
	
	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		super.deserialize();
		this.allIdentical = this.content.readBoolean();
		int nb = this.content.readShort();
		this.minimalPrices = new Vector<Integer>(nb);
		for(int i = 0; i < nb; ++i)
			this.minimalPrices.add(this.content.readVarInt());
	}
}