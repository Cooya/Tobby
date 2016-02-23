package gamedata.currentmap;

import utilities.ByteArray;

public class TaxCollectorStaticInformations {
	
	public static int protocolId = 147;
    
    public int firstNameId = 0;
    
    public int lastNameId = 0;
    
    public GuildInformations guildIdentity;
    
    public TaxCollectorStaticInformations(ByteArray buffer)
    {
    	this.firstNameId = buffer.readVarShort();
        this.lastNameId = buffer.readVarShort();
        this.guildIdentity = new GuildInformations(buffer);
    }
    
}
