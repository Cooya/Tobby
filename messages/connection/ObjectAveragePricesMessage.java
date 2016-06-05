package messages.connection;

import messages.NetworkMessage;

public class ObjectAveragePricesMessage extends NetworkMessage {
	public int[] ids;
    public int[] avgPrices;
    
	@Override
	public void serialize() {
		
	}
	
	@Override
	public void deserialize() {
		int nb = this.content.readShort();
		this.ids = new int[nb];
		for(int i = 0; i < nb; ++i)
			this.ids[i] = this.content.readVarShort();
		nb = this.content.readShort();
		this.avgPrices = new int[nb];
		for(int i = 0; i < nb; ++i)
			this.avgPrices[i] = this.content.readVarInt();
	}
}