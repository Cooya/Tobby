package messages.fight;

import messages.Message;
import utilities.ByteArray;

public class GameFightStartingMessage extends Message{
	public static final int Id = 700;

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
		if(this.fightType < 0)
		{
			throw new Error("Forbidden value (" + this.fightType + ") on element of GameFightStartingMessage.fightType.");
		}
		this.attackerId = buffer.readDouble();
		if(this.attackerId < -9.007199254740992E15 || this.attackerId > 9.007199254740992E15)
		{
			throw new Error("Forbidden value (" + this.attackerId + ") on element of GameFightStartingMessage.attackerId.");
		}
		this.defenderId = buffer.readDouble();
		if(this.defenderId < -9.007199254740992E15 || this.defenderId > 9.007199254740992E15)
		{
			throw new Error("Forbidden value (" + this.defenderId + ") on element of GameFightStartingMessage.defenderId.");
		}
	}
}
