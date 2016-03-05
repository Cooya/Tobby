package gamedata.fight;

import java.util.Vector;

import utilities.ByteArray;

public class FightLoot {
	public Vector<Integer> objects;
    public int kamas = 0;
    
    public FightLoot(ByteArray buffer) {
    	this.objects = new Vector<Integer>();
    	int nb = buffer.readShort();
    	for(int i = 0; i < nb; ++i)
    		this.objects.add(buffer.readVarShort());
    	this.kamas = buffer.readVarInt();
    }
}