package messages.fight;

import java.util.Vector;

import messages.Message;
import utilities.ByteArray;

public class GameFightTurnListMessage extends Message{
	public static final int Id = 713;

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
			if(loc6 < -9.007199254740992E15 || loc6 > 9.007199254740992E15)
			{
				throw new Error("Forbidden value (" + loc6 + ") on elements of ids.");
			}
			this.ids.add(loc6);
			loc3++;
		}
		int loc4 = buffer.readShort();
		int loc5 = 0;
		while(loc5 < loc4)
		{
			loc7 = buffer.readDouble();
			if(loc7 < -9.007199254740992E15 || loc7 > 9.007199254740992E15)
			{
				throw new Error("Forbidden value (" + loc7 + ") on elements of deadsIds.");
			}
			this.deadsIds.add(loc7);
			loc5++;
		}
	}
}
