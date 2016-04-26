package messages.exchanges;

public class ExchangeStartedWithStorageMessage extends ExchangeStartedMessage {
	public int storageMaxSlot = 0;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		super.deserialize();
		//this.storageMaxSlot = this.content.readVarInt(); // ne peut pas être lu car c'est un négatif
	}
}