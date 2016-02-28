package gamedata.inventory;

import utilities.ByteArray;

public class PresetItem 
{

	public int position = 63;

	public int objGid = 0;

	public int objUid = 0;

	public PresetItem()
	{
		super();
	}

	public int getTypeId() 
	{
		return 354;
	}

	public PresetItem initPresetItem(int buffer,int param2,int param3)
	{
		this.position = buffer;
		this.objGid = param2;
		this.objUid = param3;
		return this;
	}

	public void reset()
	{
		this.position = 63;
		this.objGid = 0;
		this.objUid = 0;
	}

	public void deserialize(ByteArray buffer)
	{
		this.position = buffer.readByte();
		this.objGid = buffer.readVarShort();
		this.objUid = buffer.readVarInt();
	}
}

