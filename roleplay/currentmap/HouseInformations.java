package roleplay.currentmap;

import java.util.Vector;

import utilities.BooleanByteWrapper;
import utilities.ByteArray;

public class HouseInformations {
    public int houseId = 0;
    public Vector<Integer> doorsOnMap;
    public String ownerName = "";
    public boolean isOnSale = false;
    public boolean isSaleLocked = false;
    public int modelId = 0;
    
    public HouseInformations(ByteArray buffer) {
    	doorsOnMap = new Vector<Integer>();
    	
    	int nb = buffer.readByte();
        this.isOnSale = BooleanByteWrapper.getFlag(nb, 0);
        this.isSaleLocked = BooleanByteWrapper.getFlag(nb, 1);
        this.houseId = buffer.readVarInt();
        int nbDoors = buffer.readShort();
        for(int i = 0; i < nbDoors; ++i)
        	this.doorsOnMap.add(buffer.readInt());
        this.ownerName = buffer.readUTF();
        this.modelId = buffer.readVarShort();
    }
}
