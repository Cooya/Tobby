package game.currentmap;

import game.ProtocolTypeManager;
import utilities.ByteArray;

public class GameRolePlayHumanoidInformations extends GameRolePlayNamedActorInformations {
    public HumanInformations humanoidInfo;
    public int accountId = 0;
    
    public GameRolePlayHumanoidInformations(ByteArray buffer) {
    	super(buffer);
    	this.humanoidInfo = (HumanInformations) ProtocolTypeManager.getInstance(buffer.readShort(), buffer);
    	this.accountId = buffer.readInt();
    }
}
