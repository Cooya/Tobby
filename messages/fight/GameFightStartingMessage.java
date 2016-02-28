package messages.fight;

import messages.Message;
import utilities.ByteArray;

public class GameFightStartingMessage extends Message{

	public int fightType = 0;

	public double attackerId = 0;

	public double defenderId = 0;

	public GameFightStartingMessage(Message msg)
	{
		super(msg);
	}

	public void deserialize()
	{
		ByteArray buffer =new ByteArray(this.getContent());
		this.fightType = buffer.readByte();
		this.attackerId = buffer.readDouble();
		this.defenderId = buffer.readDouble();
	}
}
