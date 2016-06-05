package gamedata.fight;

import utilities.ByteArray;

public class FightLoot {
	public int[] objects;
    public int kamas = 0;
    
    public FightLoot(ByteArray buffer) {
    	int nb = buffer.readShort();
    	this.objects = new int[nb];
    	for(int i = 0; i < nb; ++i)
    		this.objects[i] = buffer.readVarShort();
    	this.kamas = buffer.readVarInt();
    }
}