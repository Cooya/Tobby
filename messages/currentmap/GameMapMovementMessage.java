package messages.currentmap;

import java.util.Vector;

import messages.Message;
import utilities.ByteArray;

public class GameMapMovementMessage extends Message{
	public static final int Id = 951;

	public Vector<Integer> keyMovements;

	public double actorId = 0;

	public GameMapMovementMessage(Message msg)
	{
		super(msg);
		this.keyMovements = new  Vector<Integer>();
	}

	public void deserialize() 
	{
		ByteArray buffer =new ByteArray(this.content);
		int loc4 = 0;
	int loc2 = buffer.readShort();
	int loc3 = 0;
	while(loc3 < loc2)
	{
		loc4 = buffer.readShort();
		if(loc4 < 0)
		{
			throw new Error("Forbidden value (" + loc4 + ") on elements of keyMovements.");
		}
		this.keyMovements.add(loc4);
		loc3++;
	}
	this.actorId = buffer.readDouble();
	if(this.actorId < -9.007199254740992E15 || this.actorId > 9.007199254740992E15)
	{
		throw new Error("Forbidden value (" + this.actorId + ") on element of GameMapMovementMessage.actorId.");
	}
	}
}
