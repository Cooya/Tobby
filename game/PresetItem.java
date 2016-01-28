package game;

import utilities.ByteArray;

public class PresetItem 
{

	public static final int protocolId = 354;

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
		if(this.position < 0 || this.position > 255)
		{
			throw new Error("Forbidden value (" + this.position + ") on element of PresetItem.position.");
		}
		this.objGid = buffer.readVarShort();
		if(this.objGid < 0)
		{
			throw new Error("Forbidden value (" + this.objGid + ") on element of PresetItem.objGid.");
		}
		this.objUid = buffer.readVarInt();
		if(this.objUid < 0)
		{
			throw new Error("Forbidden value (" + this.objUid + ") on element of PresetItem.objUid.");
		}
	}
}

