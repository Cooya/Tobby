package gamedata.fight;

import utilities.ByteArray;

public class GameFightMonsterInformations extends GameFightAIInformations{

    public int creatureGenericId = 0;
    
    public int creatureGrade = 0;
    
    public GameFightMonsterInformations(ByteArray buffer)
    {
       super(buffer);
       this.creatureGenericId = buffer.readVarShort();
       if(this.creatureGenericId < 0)
       {
          throw new Error("Forbidden value (" + this.creatureGenericId + ") on element of GameFightMonsterInformations.creatureGenericId.");
       }
       this.creatureGrade = buffer.readByte();
       if(this.creatureGrade < 0)
       {
          throw new Error("Forbidden value (" + this.creatureGrade + ") on element of GameFightMonsterInformations.creatureGrade.");
       }
       
    }
    
    
}
