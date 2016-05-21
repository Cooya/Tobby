package messages.connection;

import java.util.Vector;

import messages.NetworkMessage;

public class ObjectAveragePricesMessage extends NetworkMessage {
	public Vector<Integer> ids;
    public Vector<Integer> avgPrices;
    
	@Override
	public void serialize() {
		
	}
	
	@Override
	public void deserialize() {
		this.avgPrices = new Vector<Integer>();
		int nb = this.content.readShort();
		this.ids = new Vector<Integer>(nb);
		for(int i = 0; i < nb; ++i)
			this.ids.add(this.content.readVarShort());
		nb = this.content.readShort();
		this.avgPrices = new Vector<Integer>(nb);
		for(int i = 0; i < nb; ++i)
			this.avgPrices.add(this.content.readVarInt());
	}
}