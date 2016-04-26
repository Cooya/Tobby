package messages.exchanges;

import messages.Message;

public class ExchangeReadyMessage extends Message {
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