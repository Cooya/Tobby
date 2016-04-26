package messages.exchanges;

public class ExchangeStartedWithPodsMessage extends ExchangeStartedMessage {
	public double firstCharacterId = 0;
    public int firstCharacterCurrentWeight = 0;
    public int firstCharacterMaxWeight = 0;
    public double secondCharacterId = 0;
    public int secondCharacterCurrentWeight = 0;
    public int secondCharacterMaxWeight = 0;
    
    @Override
	public void serialize() {
		// not implemented yet
	}
    
    @Override
	public void deserialize() {
		super.deserialize();
		this.firstCharacterId = this.content.readDouble();
		this.firstCharacterCurrentWeight = this.content.readVarInt();
		this.firstCharacterMaxWeight = this.content.readVarInt();
		this.secondCharacterId = this.content.readDouble();
		this.secondCharacterCurrentWeight = this.content.readVarInt();
		this.secondCharacterMaxWeight = this.content.readVarInt();
	}
}