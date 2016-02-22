package game.character;

import utilities.ByteArray;

public class AbstractCharacterInformation {
    
    public double id = 0;
    
    public AbstractCharacterInformation(ByteArray buffer)
    {
    	this.id = buffer.readVarLong().toNumber();
    }

}
