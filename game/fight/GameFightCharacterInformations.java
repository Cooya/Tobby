package game.fight;

import game.currentmap.ActorAlignmentInformations;
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
    	if(this.level < 0 || this.level > 255)
        {
           throw new Error("Forbidden value (" + this.level + ") on element of GameFightCharacterInformations.level.");
        }
        this.alignmentInfos = new ActorAlignmentInformations(buffer);
        this.breed = buffer.readByte();
        this.sex = buffer.readBoolean();
    }
    
}
