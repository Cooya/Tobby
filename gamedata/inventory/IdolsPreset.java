package gamedata.inventory;

import java.util.Vector;

import utilities.ByteArray;

public class IdolsPreset
{
	public int presetId = 0;

	public int symbolId = 0;

	public Vector<Integer> idolId;

	public  IdolsPreset()
	{
		super();
		this.idolId = new Vector<Integer>();
	}

	public int getTypeId() 
	{
		return 491;
	}

	public IdolsPreset initIdolsPreset(int buffer,int param2, Vector<Integer> param3)
	{
		this.presetId = buffer;
		this.symbolId = param2;
		this.idolId = param3;
		return this;
	}

	public void reset()
	{
		this.presetId = 0;
		this.symbolId = 0;
		this.idolId = new Vector<Integer>();
	}

	public void deserialize(ByteArray buffer) 
	{
		int loc4 = 0;
		this.presetId = buffer.readByte();
		this.symbolId = buffer.readByte();
		int loc2 = buffer.readShort();
		int loc3 = 0;
		while(loc3 < loc2)
		{
			loc4 = buffer.readVarShort();
			this.idolId.add(loc4);
			loc3++;
		}
	}
}
