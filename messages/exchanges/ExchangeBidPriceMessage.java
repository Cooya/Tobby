package messages.exchanges;

import messages.NetworkMessage;

public class ExchangeBidPriceMessage extends NetworkMessage {
	public int genericId = 0;
	public int averagePrice = 0;

	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		this.genericId = this.content.readVarShort();
        this.averagePrice = this.content.readVarInt();
	}
}