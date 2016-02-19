package game.inventory;

import java.util.Vector;

import utilities.ByteArray;

public class Preset
{

	public static final int protocolId = 355;

	public int presetId = 0;

	public int symbolId = 0;

	public boolean mount = false;

	public Vector<PresetItem> objects;

	public  Preset()
	{
		super();
		this.objects = new Vector<PresetItem>();
	}

	public int getTypeId() 
	{
		return 355;
	}

	public Preset initPreset(int buffer,int param2,boolean param3,Vector<PresetItem> param4)
	{
		this.presetId = buffer;
		this.symbolId = param2;
		this.mount = param3;
		this.objects = param4;
		return this;
	}

	public void reset()
	{
		this.presetId = 0;
		this.symbolId = 0;
		this.mount = false;
		this.objects = new Vector<PresetItem>();
	}

	public void deserialize(ByteArray buffer)
	{
		PresetItem item = null;
		this.presetId = buffer.readByte();
		if(this.presetId < 0)
		{
			throw new Error("Forbidden value (" + this.presetId + ") on element of Preset.presetId.");
		}
		this.symbolId = buffer.readByte();
		if(this.symbolId < 0)
		{
			throw new Error("Forbidden value (" + this.symbolId + ") on element of Preset.symbolId.");
		}
		this.mount = buffer.readBoolean();
		int loc2 = buffer.readShort();
		int loc3 = 0;
		while(loc3 < loc2)
		{
			item = new PresetItem();
			item.deserialize(buffer);
			this.objects.add(item);
			loc3++;
		}
	}
}

