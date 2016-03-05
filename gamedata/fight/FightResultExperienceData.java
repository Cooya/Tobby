package gamedata.fight;

import utilities.BooleanByteWrapper;
import utilities.ByteArray;

public class FightResultExperienceData extends FightResultAdditionalData {
    public double experience = 0;
    public boolean showExperience = false;
    public double experienceLevelFloor = 0;
    public boolean showExperienceLevelFloor = false;
    public double experienceNextLevelFloor = 0;
    public boolean showExperienceNextLevelFloor = false;
    public double experienceFightDelta = 0;
    public boolean showExperienceFightDelta = false;
    public double experienceForGuild = 0;
    public boolean showExperienceForGuild = false;
    public double experienceForMount = 0;
    public boolean showExperienceForMount = false;
    public boolean isIncarnationExperience = false;
    public int rerollExperienceMul = 0;
    
    public FightResultExperienceData(ByteArray buffer) {
    	super(buffer);
    	int b = buffer.readByte();
    	this.showExperience = BooleanByteWrapper.getFlag(b, 0);
    	this.showExperienceLevelFloor = BooleanByteWrapper.getFlag(b, 1);
    	this.showExperienceNextLevelFloor = BooleanByteWrapper.getFlag(b, 2);
    	this.showExperienceFightDelta = BooleanByteWrapper.getFlag(b, 3);
    	this.showExperienceForGuild = BooleanByteWrapper.getFlag(b, 4);
    	this.showExperienceForMount = BooleanByteWrapper.getFlag(b, 5);
    	this.isIncarnationExperience = BooleanByteWrapper.getFlag(b, 6);
    	this.experience = buffer.readVarLong().toNumber();
    	this.experienceLevelFloor = buffer.readVarLong().toNumber();
    	this.experienceNextLevelFloor = buffer.readVarLong().toNumber();
    	this.experienceFightDelta = buffer.readVarLong().toNumber();
    	this.experienceForGuild = buffer.readVarLong().toNumber();
    	this.experienceForMount = buffer.readVarLong().toNumber();
    	this.rerollExperienceMul = buffer.readByte();
    }
}