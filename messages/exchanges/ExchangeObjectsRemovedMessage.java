package messages.exchanges;

public class ExchangeObjectsRemovedMessage extends ExchangeObjectMessage {
	public int[] objectUID;

	@Override
	public void serialize() {
		
	}
	
	@Override
	public void deserialize() {
		super.deserialize();
		int nb = this.content.readShort();
		this.objectUID = new int[nb];
		for(int i = 0; i < nb; ++i)
			this.objectUID[i] = this.content.readVarInt();
	}
}