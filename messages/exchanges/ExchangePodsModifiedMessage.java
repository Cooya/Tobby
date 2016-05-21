package messages.exchanges;

public class ExchangePodsModifiedMessage extends ExchangeObjectMessage {
    public int currentWeight = 0;
    public int maxWeight = 0;

	@Override
	public void serialize() {
		
	}
	
	@Override
	public void deserialize() {
		super.deserialize();
		this.currentWeight = this.content.readVarInt();
        this.maxWeight = this.content.readVarInt();
	}
}