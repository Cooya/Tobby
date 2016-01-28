package roleplay.currentmap;

import utilities.ByteArray;

public class GameRolePlayHumanoidInformations extends GameRolePlayNamedActorInformations {
    public HumanInformations humanoidInfo;
    public int accountId = 0;
    
    public GameRolePlayHumanoidInformations(ByteArray buffer) {
    	super(buffer);
    	int protocolId = buffer.readShort();
    	if(protocolId == 157)
    		this.humanoidInfo = new HumanInformations(buffer);
    	else
    		throw new Error("Invalid or unhandled protocol id : " + protocolId + ".");
    	this.accountId = buffer.readInt();
    }
}
