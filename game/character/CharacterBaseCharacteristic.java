package game.character;

import utilities.ByteArray;

public class CharacterBaseCharacteristic {
    public int base = 0;
    public int additionnal = 0;
    public int objectsAndMountBonus = 0;
    public int alignGiftBonus = 0;
    public int contextModif = 0;
    
    public CharacterBaseCharacteristic(ByteArray buffer) {
    	this.base = buffer.readVarShort();
        this.additionnal = buffer.readVarShort();
        this.objectsAndMountBonus = buffer.readVarShort();
        this.alignGiftBonus = buffer.readVarShort();
        this.contextModif = buffer.readVarShort();
    }
}