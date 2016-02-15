package roleplay.fight;

import utilities.ByteArray;

public class GameFightMinimalStats {
    public int lifePoints = 0;
    
    public int maxLifePoints = 0;
    
    public int baseMaxLifePoints = 0;
    
    public int permanentDamagePercent = 0;
    
    public int shieldPoints = 0;
    
    public int actionPoints = 0;
    
    public int maxActionPoints = 0;
    
    public int movementPoints = 0;
    
    public int maxMovementPoints = 0;
    
    public double summoner = 0;
    
    public boolean summoned = false;
    
    public int neutralElementResistPercent = 0;
    
    public int earthElementResistPercent = 0;
    
    public int waterElementResistPercent = 0;
    
    public int airElementResistPercent = 0;
    
    public int fireElementResistPercent = 0;
    
    public int neutralElementReduction = 0;
    
    public int earthElementReduction = 0;
    
    public int waterElementReduction = 0;
    
    public int airElementReduction = 0;
    
    public int fireElementReduction = 0;
    
    public int criticalDamageFixedResist = 0;
    
    public int pushDamageFixedResist = 0;
    
    public int pvpNeutralElementResistPercent = 0;
    
    public int pvpEarthElementResistPercent = 0;
    
    public int pvpWaterElementResistPercent = 0;
    
    public int pvpAirElementResistPercent = 0;
    
    public int pvpFireElementResistPercent = 0;
    
    public int pvpNeutralElementReduction = 0;
    
    public int pvpEarthElementReduction = 0;
    
    public int pvpWaterElementReduction = 0;
    
    public int pvpAirElementReduction = 0;
    
    public int pvpFireElementReduction = 0;
    
    public int dodgePALostProbability = 0;
    
    public int dodgePMLostProbability = 0;
    
    public int tackleBlock = 0;
    
    public int tackleEvade = 0;
    
    public int invisibilityState = 0;
    
    public GameFightMinimalStats(ByteArray buffer)
    {
       this.lifePoints = buffer.readVarInt();
       if(this.lifePoints < 0)
       {
          throw new Error("Forbidden value (" + this.lifePoints + ") on element of GameFightMinimalStats.lifePoints.");
       }
       this.maxLifePoints = buffer.readVarInt();
       if(this.maxLifePoints < 0)
       {
          throw new Error("Forbidden value (" + this.maxLifePoints + ") on element of GameFightMinimalStats.maxLifePoints.");
       }
       this.baseMaxLifePoints = buffer.readVarInt();
       if(this.baseMaxLifePoints < 0)
       {
          throw new Error("Forbidden value (" + this.baseMaxLifePoints + ") on element of GameFightMinimalStats.baseMaxLifePoints.");
       }
       this.permanentDamagePercent = buffer.readVarInt();
       if(this.permanentDamagePercent < 0)
       {
          throw new Error("Forbidden value (" + this.permanentDamagePercent + ") on element of GameFightMinimalStats.permanentDamagePercent.");
       }
       this.shieldPoints = buffer.readVarInt();
       if(this.shieldPoints < 0)
       {
          throw new Error("Forbidden value (" + this.shieldPoints + ") on element of GameFightMinimalStats.shieldPoints.");
       }
       this.actionPoints = buffer.readVarShort();
       this.maxActionPoints = buffer.readVarShort();
       this.movementPoints = buffer.readVarShort();
       this.maxMovementPoints = buffer.readVarShort();
       this.summoner = buffer.readDouble();
       if(this.summoner < -9.007199254740992E15 || this.summoner > 9.007199254740992E15)
       {
          throw new Error("Forbidden value (" + this.summoner + ") on element of GameFightMinimalStats.summoner.");
       }
       this.summoned = buffer.readBoolean();
       this.neutralElementResistPercent = buffer.readVarInt();
       this.earthElementResistPercent = buffer.readVarInt();
       this.waterElementResistPercent = buffer.readVarInt();
       this.airElementResistPercent = buffer.readVarInt();          
       this.fireElementResistPercent = buffer.readVarInt();
       this.neutralElementReduction = buffer.readVarShort();
       this.earthElementReduction = buffer.readVarShort();
       this.waterElementReduction = buffer.readVarShort();
       this.airElementReduction = buffer.readVarShort();
       this.fireElementReduction = buffer.readVarShort();
       this.criticalDamageFixedResist = buffer.readVarShort();
       this.pushDamageFixedResist = buffer.readVarShort();
       this.pvpNeutralElementResistPercent = buffer.readVarShort();
       this.pvpEarthElementResistPercent = buffer.readVarShort();
       this.pvpWaterElementResistPercent = buffer.readVarShort();
       this.pvpAirElementResistPercent = buffer.readVarShort();
       this.pvpFireElementResistPercent = buffer.readVarShort();
       this.pvpNeutralElementReduction = buffer.readVarShort();
       this.pvpEarthElementReduction = buffer.readVarShort();
       this.pvpWaterElementReduction = buffer.readVarShort();
       this.pvpAirElementReduction = buffer.readVarShort();
       this.pvpFireElementReduction = buffer.readVarShort();
       this.dodgePALostProbability = buffer.readVarShort();
       if(this.dodgePALostProbability < 0)
       {
          throw new Error("Forbidden value (" + this.dodgePALostProbability + ") on element of GameFightMinimalStats.dodgePALostProbability.");
       }
       this.dodgePMLostProbability = buffer.readVarShort();
       if(this.dodgePMLostProbability < 0)
       {
          throw new Error("Forbidden value (" + this.dodgePMLostProbability + ") on element of GameFightMinimalStats.dodgePMLostProbability.");
       }
       this.tackleBlock = buffer.readVarShort();
       this.tackleEvade = buffer.readVarShort();
       this.invisibilityState = buffer.readByte();
       if(this.invisibilityState < 0)
       {
          throw new Error("Forbidden value (" + this.invisibilityState + ") on element of GameFightMinimalStats.invisibilityState.");
       }
    }
}
