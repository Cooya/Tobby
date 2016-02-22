package game.currentmap;

import game.ProtocolTypeManager;
import utilities.ByteArray;

public class GameRolePlayPrismInformations extends GameRolePlayActorInformations{
	
	public static final int protocolId = 161;
    
    public PrismInformation prism;

	public GameRolePlayPrismInformations(ByteArray buffer) {
		super(buffer);
        this.prism =  (PrismInformation) ProtocolTypeManager.getInstance(buffer.readShort(), buffer);
		
	}
	
	
	

}
