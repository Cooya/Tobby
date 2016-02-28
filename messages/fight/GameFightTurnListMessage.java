package messages.fight;

import java.util.Vector;

import messages.Message;
import utilities.ByteArray;

public class GameFightTurnListMessage extends Message{

	public Vector<Double> ids;

	public Vector<Double> deadsIds;

	public GameFightTurnListMessage(Message msg)
	{
		super(msg);
		this.ids = new Vector<Double>();
		this.deadsIds = new Vector<Double>();
	}

	public void deserialize()
	{
		ByteArray buffer=new ByteArray(this.getContent());
		double loc6;
		double loc7;
		int loc2 = buffer.readShort();
		int loc3 = 0;
		while(loc3 < loc2)
		{
			loc6 = buffer.readDouble();
			this.ids.add(loc6);
			loc3++;
		}
		int loc4 = buffer.readShort();
		int loc5 = 0;
		while(loc5 < loc4)
		{
			loc7 = buffer.readDouble();
			this.deadsIds.add(loc7);
			loc5++;
		}
	}
}
