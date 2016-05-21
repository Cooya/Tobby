package messages.exchanges;

import java.util.Vector;

public class ExchangeObjectsRemovedMessage extends ExchangeObjectMessage {
	public Vector<Integer> objectUID;

	@Override
	public void serialize() {
		
	}
	
	@Override
	public void deserialize() {
		super.deserialize();
		int nb = this.content.readShort();
		this.objectUID = new Vector<Integer>(nb);
		for(int i = 0; i < nb; ++i)
			this.objectUID.add(this.content.readVarInt());
	}
}