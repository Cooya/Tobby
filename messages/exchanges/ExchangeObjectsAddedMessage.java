package messages.exchanges;

import gamedata.inventory.ObjectItem;

import java.util.Vector;

public class ExchangeObjectsAddedMessage extends ExchangeObjectMessage {
	public Vector<ObjectItem> object;

	@Override
	public void serialize() {

	}

	@Override
	public void deserialize() {
		super.deserialize();
		int nb = this.content.readShort();
		this.object = new Vector<ObjectItem>(nb);
		for(int i = 0; i < nb; ++i)
			this.object.add(new ObjectItem(this.content));
	}
}