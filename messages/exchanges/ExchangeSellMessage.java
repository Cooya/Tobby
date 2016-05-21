package messages.exchanges;

import messages.NetworkMessage;

public class ExchangeSellMessage extends NetworkMessage {
	public int objectToSellId = 0;
	public int quantity = 0;

	@Override
	public void serialize() {
        this.content.writeVarInt(this.objectToSellId);
        this.content.writeVarInt(this.quantity);
	}

	@Override
	public void deserialize() {
		
	}
}