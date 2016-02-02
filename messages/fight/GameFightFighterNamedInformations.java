package messages.fight;

import roleplay.fight.GameFightFighterInformations;
import utilities.ByteArray;

public class GameFightFighterNamedInformations extends GameFightFighterInformations{
	
    public String name = "";
    
    public PlayerStatus status;
    
    public GameFightFighterNamedInformations()
    {
    	super();
       this.status = new PlayerStatus();
    }
    

    public void deserializeAs_GameFightFighterNamedInformations(ByteArray buffer)
    {
       super.deserialize(buffer);
       this.name = buffer.readUTF();
       this.status = new PlayerStatus();
       this.status.deserialize(buffer);
    }
}
