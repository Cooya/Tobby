package messages.exchanges;

import gamedata.inventory.ObjectItem;

public class ExchangeObjectsAddedMessage extends ExchangeObjectMessage {
	public ObjectItem[] object;

	@Override
	public void serialize() {

	}

	@Override
	public void deserialize() {
		super.deserialize();
		int nb = this.content.readShort();
		this.object = new ObjectItem[nb];
		for(int i = 0; i < nb; ++i)
			this.object[i] = new ObjectItem(this.content);
	}
}