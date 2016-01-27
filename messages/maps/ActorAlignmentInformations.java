package messages.maps;

import utilities.ByteArray;

public class ActorAlignmentInformations {
    public int alignmentSide = 0;
    public int alignmentValue = 0;
    public int alignmentGrade = 0;
    public double characterPower = 0;
    
    public ActorAlignmentInformations(ByteArray buffer) {
        this.alignmentSide = buffer.readByte();
        this.alignmentValue = buffer.readByte();
        this.alignmentGrade = buffer.readByte();
        this.characterPower = buffer.readDouble();
    }
}