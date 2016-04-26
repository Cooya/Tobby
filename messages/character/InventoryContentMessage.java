package messages.character;

import gamedata.inventory.ObjectItem;

import java.util.Vector;

import messages.Message;

public class InventoryContentMessage extends Message {
    public Vector<ObjectItem> objects;
    public int kamas = 0;
    
    @Override
	public void serialize() {
		// not implemented yet
	}
    
    @Override
    public void deserialize() {
    	this.objects = new Vector<ObjectItem>();
    	int nb = this.content.readShort();
    	for(int i = 0; i < nb; ++i)
    		this.objects.add(new ObjectItem(this.content));
    	this.kamas = this.content.readVarInt();
    }
}