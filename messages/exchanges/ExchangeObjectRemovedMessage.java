package messages.exchanges;

public class ExchangeObjectRemovedMessage extends ExchangeObjectMessage {
	public int objectUID;

	@Override
	public void serialize() {
		
	}
	
	@Override
	public void deserialize() {
		super.deserialize();
		this.objectUID = this.content.readVarInt();
	}
}