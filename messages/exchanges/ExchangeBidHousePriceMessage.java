package messages.exchanges;

import messages.NetworkMessage;

public class ExchangeBidHousePriceMessage extends NetworkMessage {
	public int genId = 0;

	@Override
	public void serialize() {
		this.content.writeVarShort(this.genId);
	}
	
	@Override
	public void deserialize() {
		
	}
}