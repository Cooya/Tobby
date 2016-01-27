package messages.maps;

import utilities.ByteArray;

public class GameRolePlayHumanoidInformations extends GameRolePlayNamedActorInformations {
    public HumanInformations humanoidInfo;
    public int accountId = 0;
    
    public GameRolePlayHumanoidInformations(ByteArray buffer) {
    	super(buffer);
    	buffer.readShort(); // id du message HumanInformations
    	this.humanoidInfo = new HumanInformations(buffer);
    	this.accountId = buffer.readInt();
    }
}
