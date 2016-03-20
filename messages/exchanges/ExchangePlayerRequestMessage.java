package messages.exchanges;

import main.Emulation;
import utilities.ByteArray;

public class ExchangePlayerRequestMessage extends ExchangeRequestMessage {
	public double target;

	public ExchangePlayerRequestMessage() {
		super();
	}

	public void serialize(double target, int exchangeType, int instanceId) {
		ByteArray buffer = new ByteArray();
		super.serialize(buffer, exchangeType);
		buffer.writeVarLong(target);
		completeInfos(Emulation.hashMessage(buffer, instanceId));
	}
}