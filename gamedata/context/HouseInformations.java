package gamedata.context;

import utilities.BooleanByteWrapper;
import utilities.ByteArray;

public class HouseInformations {
	public int houseId = 0;
	public int[] doorsOnMap;
	public String ownerName = "";
	public boolean isOnSale = false;
	public boolean isSaleLocked = false;
	public int modelId = 0;

	public HouseInformations(ByteArray buffer) {   	
		int nb = buffer.readByte();
		this.isOnSale = BooleanByteWrapper.getFlag(nb, 0);
		this.isSaleLocked = BooleanByteWrapper.getFlag(nb, 1);
		this.houseId = buffer.readVarInt();
		nb = buffer.readShort();
    	this.doorsOnMap = new int[nb];
		for(int i = 0; i < nb; ++i)
			this.doorsOnMap[i] = buffer.readInt();
		this.ownerName = buffer.readUTF();
		this.modelId = buffer.readVarShort();
	}
}