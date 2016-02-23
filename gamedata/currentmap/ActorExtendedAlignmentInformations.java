package gamedata.currentmap;

import utilities.ByteArray;

public class ActorExtendedAlignmentInformations extends ActorAlignmentInformations {
    public int honor = 0;
    public int honorGradeFloor = 0;
    public int honorNextGradeFloor = 0;
    public int aggressable = 0;
    
    public ActorExtendedAlignmentInformations(ByteArray buffer) {
    	super(buffer);
    	this.honor = buffer.readVarShort();
    	this.honorGradeFloor = buffer.readVarShort();
    	this.honorNextGradeFloor = buffer.readVarShort();
    	this.aggressable = buffer.readByte();
    }
}