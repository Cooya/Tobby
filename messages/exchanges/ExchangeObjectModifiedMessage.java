package messages.exchanges;

import gamedata.inventory.ObjectItem;

public class ExchangeObjectModifiedMessage extends ExchangeObjectMessage {
	public ObjectItem object;

	@Override
	public void serialize() {
		
	}
	
	@Override
	public void deserialize() {
		super.deserialize();
		this.object = new ObjectItem(this.content);
	}
}