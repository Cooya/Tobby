package messages.exchanges;

import java.util.Vector;

import gamedata.inventory.ObjectItem;

public class ExchangeObjectsModifiedMessage extends ExchangeObjectMessage {
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