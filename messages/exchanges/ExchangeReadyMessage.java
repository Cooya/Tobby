package messages.exchanges;

import messages.NetworkMessage;

public class ExchangeReadyMessage extends NetworkMessage {
	public boolean ready = false;
	public int step = 0;
    
	@Override
    public void serialize() {
       this.content.writeBoolean(this.ready);
       this.content.writeVarShort(this.step);
    }
    
    @Override
	public void deserialize() {
		// not implemented yet
	}
}