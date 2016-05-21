package messages.exchanges;

import messages.NetworkMessage;

public class ExchangeObjectMoveMessage extends NetworkMessage {
	public int objectUID = 0;
	public int quantity = 0;
	
	@Override
	public void serialize() {
		this.content.writeVarInt(this.objectUID);
        this.content.writeVarInt(this.quantity);
	}
	
	@Override
	public void deserialize() {
		
	}
}