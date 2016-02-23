package messages.exchange;

import messages.Message;
import utilities.ByteArray;

public class ExchangeStartedWithPodsMessage extends ExchangeStartedMessage {
	public double firstCharacterId = 0;
    public int firstCharacterCurrentWeight = 0;
    public int firstCharacterMaxWeight = 0;
    public double secondCharacterId = 0;
    public int secondCharacterCurrentWeight = 0;
    public int secondCharacterMaxWeight = 0;
    
    public ExchangeStartedWithPodsMessage(Message msg) {
		super(msg);
		deserialize();
	}
    
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		super.deserialize(buffer);
		this.firstCharacterId = buffer.readDouble();
		this.firstCharacterCurrentWeight = buffer.readVarInt();
		this.firstCharacterMaxWeight = buffer.readVarInt();
		this.secondCharacterId = buffer.readDouble();
		this.secondCharacterCurrentWeight = buffer.readVarInt();
		this.secondCharacterMaxWeight = buffer.readVarInt();
	}
}