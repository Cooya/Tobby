package gamedata.bid;

import utilities.ByteArray;

public class SellerBuyerDescriptor {
	public int[] quantities;
    public int[] types;
    public double taxPercentage = 0;
    public double taxModificationPercentage = 0;
    public int maxItemLevel = 0;
    public int maxItemPerAccount = 0;
    public int npcContextualId = 0;
    public int unsoldDelay = 0;
    
    public SellerBuyerDescriptor(ByteArray buffer) {
    	int nb = buffer.readShort();
    	this.quantities = new int[nb];
    	for(int i = 0; i < nb; ++i)
    		this.quantities[i] = buffer.readVarInt();
    	nb = buffer.readShort();
    	this.types = new int[nb];
    	for(int i = 0; i < nb; ++i)
    		this.types[i] = buffer.readVarInt();
    	this.taxPercentage = buffer.readFloat();
        this.taxModificationPercentage = buffer.readFloat();
        this.maxItemLevel = buffer.readByte();
        this.maxItemPerAccount = buffer.readVarInt();
        this.npcContextualId = buffer.readInt();
        this.unsoldDelay = buffer.readVarShort();
    }
}