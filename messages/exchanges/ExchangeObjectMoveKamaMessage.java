package messages.exchanges;

import utilities.ByteArray;
import messages.Message;

public class ExchangeObjectMoveKamaMessage extends Message {
	public int quantity = 0;
	
	public ExchangeObjectMoveKamaMessage() {
		super();
	}
	
	public void serialize(int quantity) {
		this.quantity = quantity;
		
		ByteArray buffer = new ByteArray();
		buffer.writeVarInt(this.quantity);
		super.completeInfos(buffer);
	}
}