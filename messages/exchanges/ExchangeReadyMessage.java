package messages.exchanges;

import messages.Message;
import utilities.ByteArray;

public class ExchangeReadyMessage extends Message {
    
    public ExchangeReadyMessage() {
       super();
    }
    
    public void serialize(boolean ready, int step) {
	   ByteArray buffer = new ByteArray();
       buffer.writeBoolean(ready);
       buffer.writeVarShort(step);
       completeInfos(buffer);
    }
}