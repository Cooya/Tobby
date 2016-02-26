package messages.fight;

import messages.Message;
import utilities.ByteArray;

public class GameActionFightPointsVariationMessage extends AbstractGameActionMessage{

	public static final int Id = 1030;

	public double targetId = 0;

	public int delta = 0;

	public GameActionFightPointsVariationMessage (Message msg){
		super(msg);
	}

	public void deserialize()
	{
		ByteArray buffer=new ByteArray(this.content);
		super.deserialize(buffer);
		this.targetId = buffer.readDouble();
		this.delta = buffer.readShort();
	}


}
