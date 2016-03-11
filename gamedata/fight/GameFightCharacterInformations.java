package gamedata.fight;

import gamedata.context.ActorAlignmentInformations;
import utilities.ByteArray;

public class GameFightCharacterInformations extends GameFightFighterNamedInformations{

    public int level = 0;
    
    public ActorAlignmentInformations alignmentInfos;
    
    public int breed = 0;
    
    public boolean sex = false;
    
    public GameFightCharacterInformations(ByteArray buffer)
    {
    	super(buffer);
    	this.level=buffer.readByte();
        this.alignmentInfos = new ActorAlignmentInformations(buffer);
        this.breed = buffer.readByte();
        this.sex = buffer.readBoolean();
    }
    
}
