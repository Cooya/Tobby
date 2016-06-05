package messages.exchanges;

public class ExchangeBidPriceForSellerMessage extends ExchangeBidPriceMessage {
	public boolean allIdentical = false;
	public int[] minimalPrices;
	
	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		super.deserialize();
		this.allIdentical = this.content.readBoolean();
		int nb = this.content.readShort();
		this.minimalPrices = new int[nb];
		for(int i = 0; i < nb; ++i)
			this.minimalPrices[i] = this.content.readVarInt();
	}
}