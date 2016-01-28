package messages.maps;

import utilities.ByteArray;

public class GameRolePlayHumanoidInformations extends GameRolePlayNamedActorInformations {
    public HumanInformations humanoidInfo;
    public int accountId = 0;
    
    public GameRolePlayHumanoidInformations(ByteArray buffer) {
    	super(buffer);
    	if(buffer.readShort() != 157) // HumanInformations
    		throw new Error("Invalid value read.");
    	this.humanoidInfo = new HumanInformations(buffer);
    	this.accountId = buffer.readInt();
    }
}
