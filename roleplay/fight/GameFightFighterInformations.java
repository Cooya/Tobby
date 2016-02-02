package roleplay.fight;

import java.util.Vector;

import roleplay.currentmap.EntityDispositionInformations;
import roleplay.currentmap.EntityLook;
import roleplay.currentmap.GameContextActorInformations;
import utilities.ByteArray;

public class GameFightFighterInformations extends GameContextActorInformations{
    public int teamId = 2;
    
    public int wave = 0;
    
    public boolean alive = false;
    
    public GameFightMinimalStats stats;
    
    public Vector<Integer> previousPositions;
    
    public GameFightFighterInformations()
    {
       this.stats = new GameFightMinimalStats();
       this.previousPositions = new Vector<Integer>();
       
    }
    
    
    public void deserialize(ByteArray buffer) 
    {
       int loc5 = 0;
       this.contextualId = buffer.readDouble();
       this.look = new EntityLook(buffer);
       int protocolId = buffer.readShort();
       if(protocolId == 60)
       	this.disposition = new EntityDispositionInformations(buffer);
       else
       	throw new Error("Invalid or unhandled protocol id : " + protocolId + ".");
       
       this.teamId = buffer.readByte();
       if(this.teamId < 0)
       {
          throw new Error("Forbidden value (" + this.teamId + ") on element of GameFightFighterInformations.teamId.");
       }
       this.wave = buffer.readByte();
       if(this.wave < 0)
       {
          throw new Error("Forbidden value (" + this.wave + ") on element of GameFightFighterInformations.wave.");
       }
       this.alive = buffer.readBoolean();
       int loc2 = buffer.readShort();
       this.stats = new GameFightMinimalStatsPreparation();
       this.stats.deserialize(buffer);
       int loc3 = buffer.readShort();
       int loc4 = 0;
       while(loc4 < loc3)
       {
          loc5 = buffer.readVarShort();
          if(loc5 < 0 || loc5 > 559)
          {
             throw new Error("Forbidden value (" + loc5 + ") on elements of previousPositions.");
          }
          this.previousPositions.add(loc5);
          loc4++;
       }
    }
}
