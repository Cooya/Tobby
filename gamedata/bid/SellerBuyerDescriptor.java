package gamedata.bid;

import java.util.Vector;

import utilities.ByteArray;

public class SellerBuyerDescriptor {
	public Vector<Integer> quantities;
    public Vector<Integer> types;
    public double taxPercentage = 0;
    public double taxModificationPercentage = 0;
    public int maxItemLevel = 0;
    public int maxItemPerAccount = 0;
    public int npcContextualId = 0;
    public int unsoldDelay = 0;
    
    public SellerBuyerDescriptor(ByteArray buffer) {
    	int nb = buffer.readShort();
    	this.quantities = new Vector<Integer>(nb);
    	for(int i = 0; i < nb; ++i)
    		this.quantities.add(buffer.readVarInt());
    	nb = buffer.readShort();
    	this.types = new Vector<Integer>(nb);
    	for(int i = 0; i < nb; ++i)
    		this.types.add(buffer.readVarInt());
    	this.taxPercentage = buffer.readFloat();
        this.taxModificationPercentage = buffer.readFloat();
        this.maxItemLevel = buffer.readByte();
        this.maxItemPerAccount = buffer.readVarInt();
        this.npcContextualId = buffer.readInt();
        this.unsoldDelay = buffer.readVarShort();
    }
}