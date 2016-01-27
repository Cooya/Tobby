package messages.game;

import java.util.Vector;

import game.IdolsPreset;
import game.ObjectItem;
import game.Preset;
import messages.Message;
import utilities.ByteArray;

public class InventoryContentAndPresetMessage extends Message{
	public static final int ID = 3016;
	public Vector<ObjectItem> inventory;
	public int kamas;
	public Vector<Preset> presets;
	public Vector<IdolsPreset> idolsPresets;



	public InventoryContentAndPresetMessage(Message msg) {
		super(msg);
		inventory=new Vector<ObjectItem>();
		presets=new Vector<Preset>();
		idolsPresets=new Vector<IdolsPreset>();
		ByteArray buffer=new ByteArray(msg.getContent());
		deserialize(buffer);
	}

	private void deserialize(ByteArray buffer) {
		deserializeInventory(buffer);
		deserializePresets(buffer);
	}



	public void deserializeInventory(ByteArray buffer){
		ObjectItem obj = null;
		int loc2 = buffer.readShort();
		int loc3 = 0;
		System.out.println("Il y a "+loc2+" items");
		while(loc3 < loc2)
		{
			obj = new ObjectItem();
			obj.deserialize(buffer);
			this.inventory.add(obj);
			loc3++;
		}
		this.kamas = buffer.readVarInt();
		if(this.kamas < 0)
		{
			throw new Error("Forbidden value (" + this.kamas + ") on element of InventoryContentMessage.kamas.");
		}
	}



	public void deserializePresets(ByteArray buffer){
		Preset loc6 = null;
		IdolsPreset  loc7 = null;
		int loc2 = buffer.readShort();
		int loc3 = 0;
		while(loc3 < loc2)
		{
			loc6 = new Preset();
			loc6.deserialize(buffer);
			this.presets.add(loc6);
			loc3++;
		}
		int loc4 = buffer.readShort();
		int loc5 = 0;
		while(loc5 < loc4)
		{
			loc7 = new IdolsPreset();
			loc7.deserialize(buffer);
			this.idolsPresets.add(loc7);
			loc5++;
		}
	}





}
